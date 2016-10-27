package chat.rocket.android;

import android.util.Log;

import bolts.Continuation;
import bolts.Task;

public class Constants {
    public static final String LOG_TAG = "Rocket.Chat.Android";

    public static final String AUTHORITY = "chat.rocket.android";

    public static final Continuation ERROR_LOGGING = new Continuation() {
        @Override
        public Object then(Task task) throws Exception {
            if (task.isFaulted()) {
                Log.e(LOG_TAG, "error", task.getError());
            }
            return null;
        }
    };
}
