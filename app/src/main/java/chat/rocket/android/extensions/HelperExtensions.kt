package chat.rocket.android.extensions

import chat.rocket.android.BuildConfig

fun Throwable.printStackTraceOnDebug() {
    if (BuildConfig.DEBUG) {
        this.printStackTrace()
    }
}