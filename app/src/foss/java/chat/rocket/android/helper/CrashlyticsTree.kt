package chat.rocket.android.helper

import timber.log.Timber
import android.util.Log

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        Log.println(priority, tag, message)

        if (throwable != null) {
            Log.e(tag,throwable.toString())
        }
    }
}
