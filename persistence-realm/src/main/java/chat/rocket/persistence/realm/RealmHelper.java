package chat.rocket.persistence.realm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import org.json.JSONException;

import java.util.Collections;
import java.util.List;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.log.RCLog;

@SuppressLint("NewApi")
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

  public <T extends RealmObject> T executeTransactionForRead(Transaction<T> transaction) {
    try (Realm realm = instance()) {
      T source = transaction.execute(realm);
      return source != null ? realm.copyFromRealm(source) : null;
    } catch (Exception exception) {
      RCLog.w(exception);
      return null;
    }
  }

  public <T extends RealmObject> List<T> executeTransactionForReadResults(
      Transaction<RealmResults<T>> transaction) {
    try (Realm realm = instance()) {
      return realm.copyFromRealm(transaction.execute(realm));
    } catch (Exception exception) {
      RCLog.w(exception);
      return Collections.emptyList();
    }
  }

  public interface Query<T extends RealmObject> {
    RealmQuery<T> query(Realm realm);
  }

  public <T extends RealmObject> boolean isObjectExists(Query<T> query) {
    try (Realm realm = instance()) {
      return query.query(realm).count() > 0;
    } catch (Exception exception) {
      RCLog.w(exception);
      return false;
    }
  }

  private boolean shouldUseSync() {
    // ref: realm-java:realm/realm-library/src/main/java/io/realm/AndroidNotifier.java
    // #isAutoRefreshAvailable()

    if (Looper.myLooper() == null) {
      return true;
    }

    String threadName = Thread.currentThread().getName();
    return threadName != null && threadName.startsWith("IntentService[");
  }

  public Task<Void> executeTransaction(final RealmHelper.Transaction transaction) {
    return shouldUseSync() ? executeTransactionSync(transaction)
        : executeTransactionAsync(transaction);
  }

  private Task<Void> executeTransactionSync(final RealmHelper.Transaction transaction) {
    final TaskCompletionSource<Void> task = new TaskCompletionSource<>();

    try (Realm realm = instance()) {
      realm.executeTransaction(_realm -> {
        try {
          transaction.execute(_realm);
        } catch (JSONException exception) {
          throw new RuntimeException(exception);
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
    realm.executeTransactionAsync(_realm -> {
      try {
        transaction.execute(_realm);
      } catch (JSONException exception) {
        throw new RuntimeException(exception);
      }
    }, () -> {
      realm.close();
      task.setResult(null);
    }, error -> {
      realm.close();
      if (error instanceof Exception) {
        task.setError((Exception) error);
      } else {
        task.setError(new Exception(error));
      }
    });

    return task.getTask();
  }

  public <T extends RealmObject> RealmListObserver<T> createListObserver(
      RealmListObserver.Query<T> query) {
    return new RealmListObserver<T>(this, query);
  }

  public <T extends RealmObject> RealmObjectObserver<T> createObjectObserver(
      RealmHelper.Query<T> query) {
    return new RealmObjectObserver<T>(this, query);
  }

  public <T extends RealmObject> RealmAutoCompleteAdapter<T> createAutoCompleteAdapter(
      Context context,
      RealmAutoCompleteAdapter.RealmFilter<T> filter,
      RealmAutoCompleteAdapter.Constructor constructor) {
    return constructor.getNewInstance(context).initializeWith(this, filter);
  }

  public interface Transaction<T> {
    T execute(Realm realm) throws JSONException;
  }
}
