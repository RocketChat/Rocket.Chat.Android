package chat.rocket.android.helper

import timber.log.Timber

// Production logger... Just ignore it
class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        throwable?.printStackTrace()
    }
}
