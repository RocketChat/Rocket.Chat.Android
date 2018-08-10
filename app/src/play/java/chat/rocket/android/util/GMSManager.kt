package chat.rocket.android.util

import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.main.presentation.MainPresenter
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

suspend fun refreshFCMToken(presenter: MainPresenter) {
    try {
        val token = FirebaseInstanceId.getInstance().token
        Timber.d("FCM token: $token")
        presenter.refreshToken(token)
    } catch (ex: Exception) {
        Timber.d(ex, "Missing play services...")
    }
}

fun invalidateFirebaseToken(token: String) {
    FirebaseInstanceId.getInstance().deleteToken(token, FirebaseMessaging.INSTANCE_ID_SCOPE)
}
