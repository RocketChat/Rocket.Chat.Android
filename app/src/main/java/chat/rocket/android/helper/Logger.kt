package chat.rocket.android.helper

import com.crashlytics.android.Crashlytics
import com.google.firebase.crash.FirebaseCrash

import chat.rocket.android.BuildConfig

object Logger {

    fun report(throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }
        FirebaseCrash.report(throwable)
        Crashlytics.logException(throwable)
    }
}
