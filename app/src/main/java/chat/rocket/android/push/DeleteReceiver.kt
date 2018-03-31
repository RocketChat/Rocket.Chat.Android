package chat.rocket.android.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * BroadcastReceiver for dismissed notifications.
 */
class DeleteReceiver : BroadcastReceiver() {

    @Inject
    lateinit var groupedPushes: GroupedPush

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val notId = intent.extras?.getInt(EXTRA_NOT_ID)
        val host = intent.extras?.getString(EXTRA_HOSTNAME)
        if (host != null && notId != null) {
            clearNotificationsByHostAndNotificationId(host, notId)
        }
    }

    /**
     * Clear notifications by the host they belong to and its unique id.
     */
    fun clearNotificationsByHostAndNotificationId(host: String, notificationId: Int) {
        if (groupedPushes.hostToPushMessageList.isNotEmpty()) {
            val notifications = groupedPushes.hostToPushMessageList[host]
            notifications?.let {
                notifications.removeAll {
                    it.notificationId.toInt() == notificationId
                }
            }
        }
    }
}