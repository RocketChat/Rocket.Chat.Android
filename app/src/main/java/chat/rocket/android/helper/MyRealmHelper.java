package chat.rocket.android.helper;

import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

public class MyRealmHelper {
    public static <T> Task<Void> executeTransaction(final RealmHelperBolts.Transaction<T> transaction) {
        final TaskCompletionSource<Void> task = new TaskCompletionSource<>();
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        transaction.execute(realm);
                    } catch (Exception e) {
                        task.setError(e);
                    }
                }
            });
            task.trySetResult(null);
        }
        finally {
            if (!realm.isClosed()) realm.close();
        }
        return task.getTask();
    }
}
