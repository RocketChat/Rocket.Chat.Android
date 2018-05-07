package chat.rocket.android.push

import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService
import dagger.android.AndroidInjection
import javax.inject.Inject

class GcmListenerService : GcmListenerService() {

    @Inject
    lateinit var pushManager: PushManager

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        data?.let {
            pushManager.handle(data)
        }
    }
}