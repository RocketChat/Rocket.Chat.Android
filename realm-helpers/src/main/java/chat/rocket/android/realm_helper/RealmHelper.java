package chat.rocket.android.realm_helper;

import android.content.Context;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import timber.log.Timber;

public class RealmHelper {
  private final RealmConfiguration realmConfiguration;

  /*package*/ RealmHelper() {
    this.realmConfiguration = null;
  }

  /*package*/ RealmHelper(RealmConfiguration realmConfiguration) {
    this.realmConfiguration = realmConfiguration;
  }

  /*package*/ Realm instance() {
    return realmConfiguration == null
        ? Realm.getDefaultInstance() : Realm.getInstance(realmConfiguration);
  }

  public <E extends RealmObject> List<E> copyFromRealm(Iterable<E> objects) {
    if (objects == null) {
      return Collections.emptyList();
    }

    try (Realm realm = instance()) {
      return realm.copyFromRealm(objects);
    }
  }

  public <E extends RealmObject> E copyFromRealm(E object) {
    if (object == null) {
      return null;
    }

    try (Realm realm = instance()) {
      return realm.copyFromRealm(object);
    }
  }

  public interface Transaction<T> {
    T execute(Realm realm) throws JSONException;
  }

  public <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
    try (Realm realm = instance()) {
      T source = transaction.execute(realm);
      return source != null ? realm.copyFromRealm(source) : null;
    } catch (Exception exception) {
      Timber.w(exception);
      return null;
    }
  }

  public <T extends RealmObject> List<T> executeTransactionForReadResults(
      Transaction<RealmResults<T>> transaction) {
    try (Realm realm = instance()) {
      return realm.copyFromRealm(transaction.execute(realm));
    } catch (Exception exception) {
      Timber.w(exception);
      return Collections.emptyList();
    }
  }

  public Task<Void> executeTransaction(final RealmHelper.Transaction transaction) {
    return Looper.myLooper() == null ? executeTransactionSync(transaction)
        : executeTransactionAsync(transaction);
  }

  private Task<Void> executeTransactionSync(final RealmHelper.Transaction transaction) {
    final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

    try (Realm realm = instance()) {
      realm.executeTransaction(new Realm.Transaction() {
        @Override public void execute(Realm realm) {
          try {
            transaction.execute(realm);
          } catch (JSONException exception) {
            throw new RuntimeException(exception);
          }
        }
      });
      task.setResult(null);
    } catch (Exception exception) {
      task.setError(exception);
    }

    return task.getTask();
  }

  private Task<Void> executeTransactionAsync(final RealmHelper.Transaction transaction) {
    final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

    final Realm realm = instance();
    realm.executeTransactionAsync(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        try {
          transaction.execute(realm);
        } catch (JSONException exception) {
          throw new RuntimeException(exception);
        }
      }
    }, new Realm.Transaction.OnSuccess() {
      @Override public void onSuccess() {
        realm.close();
        task.setResult(null);
      }
    }, new Realm.Transaction.OnError() {
      @Override public void onError(Throwable error) {
        realm.close();
        if (error instanceof Exception) {
          task.setError((Exception) error);
        } else {
          task.setError(new Exception(error));
        }
      }
    });

    return task.getTask();
  }

  public <T extends RealmObject> RealmListObserver<T> createListObserver(
      RealmListObserver.Query<T> query) {
    return new RealmListObserver<T>(this, query);
  }

  public <T extends RealmObject> RealmObjectObserver<T> createObjectObserver(
      RealmObjectObserver.Query<T> query) {
    return new RealmObjectObserver<T>(this, query);
  }

  public <T extends RealmObject, VM, VH extends RealmModelViewHolder<VM>>
  RecyclerView.Adapter<VH> createListAdapter(Context context, RealmListObserver.Query<T> query,
    RealmModelListAdapter.Constructor<T, VM, VH> constructor) {
    return constructor.getNewInstance(context).initializeWith(this, query);
  }
}
