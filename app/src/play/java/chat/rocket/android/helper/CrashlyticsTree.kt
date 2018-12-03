package chat.rocket.android.helper

import com.crashlytics.android.Crashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        Crashlytics.log(message)

        if (throwable != null) {
            Crashlytics.logException(throwable)
        }
    }
}