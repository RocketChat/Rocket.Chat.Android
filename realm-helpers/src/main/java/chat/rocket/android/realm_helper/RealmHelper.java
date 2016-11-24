package chat.rocket.android.realm_helper;

import android.os.Looper;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.realm_adapter.RealmModelListAdapter;
import chat.rocket.android.realm_adapter.RealmModelViewHolder;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;
import java.util.Collections;
import java.util.List;
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

    Realm realm = instance();
    List<E> list = realm.copyFromRealm(objects);
    if (!realm.isClosed()) {
      realm.close();
    }
    return list;
  }

  public <E extends RealmObject> E copyFromRealm(E object) {
    if (object == null) {
      return null;
    }

    Realm realm = instance();
    E element = realm.copyFromRealm(object);
    if (!realm.isClosed()) {
      realm.close();
    }
    return element;
  }

  public interface Transaction<T> {
    T execute(Realm realm) throws Exception;
  }

  public <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
    Realm realm = instance();

    T object;

    try {
      T source = transaction.execute(realm);
      object = source != null ? realm.copyFromRealm(source) : null;
    } catch (Exception exception) {
      Timber.w(exception);
      object = null;
    } finally {
      if (!realm.isClosed()) {
        realm.close();
      }
    }

    return object;
  }

  public <T extends RealmObject> List<T> executeTransactionForReadResults(
      Transaction<RealmResults<T>> transaction) {
    Realm realm = instance();

    List<T> object;

    try {
      object = realm.copyFromRealm(transaction.execute(realm));
    } catch (Exception exception) {
      Timber.w(exception);
      object = null;
    } finally {
      if (!realm.isClosed()) {
        realm.close();
      }
    }

    return object;
  }

  public Task<Void> executeTransaction(final RealmHelper.Transaction transaction) {
    return Looper.myLooper() == null ? executeTransactionSync(transaction)
        : executeTransactionAsync(transaction);
  }

  private Task<Void> executeTransactionSync(final RealmHelper.Transaction transaction) {
    final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

    final Realm realm = instance();
    realm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        try {
          transaction.execute(realm);
          task.setResult(null);
        } catch (Exception exception) {
          task.setError(exception);
        }
      }
    });
    if (!realm.isClosed()) {
      realm.close();
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
        } catch (Exception exception) {
          task.setError(exception);
          if (!realm.isClosed()) {
            realm.close();
          }
        }
      }
    }, new Realm.Transaction.OnSuccess() {
      @Override public void onSuccess() {
        if (task.trySetResult(null)) {
          if (realm != null && !realm.isClosed()) {
            realm.close();
          }
        }
      }
    }, new Realm.Transaction.OnError() {
      @Override public void onError(Throwable error) {
        if (task.trySetError(new Exception(error))) {
          if (!realm.isClosed()) {
            realm.close();
          }
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

  public <T extends RealmObject, VH extends RealmModelViewHolder<T>> void bindListView(
      RealmModelListView listView,
      RealmModelListAdapter.Query<T> query,
      RealmModelListAdapter.Constructor<T, VH> constructor) {
    if (listView != null) {
      listView.setup(this, query, constructor);
    }
  }
}
