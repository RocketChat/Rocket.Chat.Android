package chat.rocket.android.push

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.sendMessage
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * BroadcastReceiver for direct reply on notifications.
 */
class DirectReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var factory: RocketChatClientFactory
    @Inject
    lateinit var manager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        if (ACTION_REPLY == intent.action) {
            val message = intent.getParcelableExtra<PushManager.PushMessage>(EXTRA_PUSH_MESSAGE)
            message?.let {
                launch {
                    val notificationId = it.notificationId.toInt()
                    try {
                        sendMessage(it, extractReplyMessage(intent))
                        manager.cancel(notificationId)
                    } catch (ex: RocketChatException) {
                        Timber.e(ex)
                    }
                }
            }
        }
    }

    private fun extractReplyMessage(intent: Intent): CharSequence? {
        val bundle = RemoteInput.getResultsFromIntent(intent)
        bundle?.let {
            return bundle.getCharSequence(REMOTE_INPUT_REPLY)
        }
        return null
    }

    private suspend fun sendMessage(
        pushMessage: PushManager.PushMessage,
        messageToSend: CharSequence?
    ) {
        messageToSend?.let { replyText ->
            val currentServer = pushMessage.info.hostname
            val roomId = pushMessage.info.roomId
            val client = factory.create(currentServer)
            val id = UUID.randomUUID().toString()
            client.sendMessage(id, roomId, replyText.toString())
        }
    }
}