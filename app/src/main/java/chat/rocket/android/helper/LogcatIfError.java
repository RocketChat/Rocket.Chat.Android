package chat.rocket.android.helper;

import bolts.Continuation;
import bolts.Task;
import timber.log.Timber;

public class LogcatIfError implements Continuation {
    @Override
    public Object then(Task task) throws Exception {
        if (task.isFaulted()) {
            Timber.w(task.getError());
        }
        return task;
    }
}
