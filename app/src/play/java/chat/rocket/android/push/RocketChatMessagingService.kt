package chat.rocket.android.push

import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class RocketChatMessagingService : FirebaseMessagingService() {
    @Inject lateinit var pushManager: PushManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Bundle().apply {
            message.data.entries.forEach { putString(it.key, it.value) }
            pushManager.handle(this)
        }
    }

    override fun onNewToken(token: String) {
        pushManager.registerPushNotificationToken(token)
    }
}