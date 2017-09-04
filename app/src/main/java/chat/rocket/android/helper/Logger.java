package chat.rocket.android.helper;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.crash.FirebaseCrash;

public class Logger {

    public static void report(Throwable throwable) {
        FirebaseCrash.report(throwable);
        Crashlytics.logException(throwable);
    }
}
