package chat.rocket.android.util

import chat.rocket.common.util.PlatformLogger
import timber.log.Timber

object TimberLogger : PlatformLogger {

    override fun debug(s: String) {
        Timber.d(s)
    }

    override fun info(s: String) {
        Timber.i(s)
    }

    override fun warn(s: String) {
        Timber.w(s)
    }
}