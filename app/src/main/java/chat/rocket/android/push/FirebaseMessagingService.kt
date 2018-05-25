package chat.rocket.android.push

import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushManager: PushManager

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data?.let {
            pushManager.handle(bundleOf(*(it.map { Pair(it.key, it.value) }).toTypedArray()))
        }
    }
}