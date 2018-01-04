package chat.rocket.android.push

import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService

class GcmListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        data?.let {
            PushManager.handle(this, data)
        }
    }
}