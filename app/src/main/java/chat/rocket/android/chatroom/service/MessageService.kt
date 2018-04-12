package chat.rocket.android.chatroom.service

import android.app.job.JobParameters
import android.app.job.JobService
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.sendMessage
import chat.rocket.core.model.Message
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class MessageService : JobService() {
    @Inject
    lateinit var factory: ConnectionManagerFactory
    @Inject
    lateinit var currentServerRepository: CurrentServerRepository
    @Inject
    lateinit var messageRepository: MessagesRepository

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        launch(CommonPool) {
            val currentServer = currentServerRepository.get()
            if (currentServer != null) {
                params?.let {
                    try {
                        retrySendingMessages(currentServer)
                        jobFinished(params, false)
                    } catch (ex: RocketChatException) {
                        Timber.e(ex)
                        jobFinished(params, true)
                    }
                }
            }
        }
        return true
    }

    private suspend fun retrySendingMessages(currentServer: String) {
        val temporaryMessages = messageRepository.getAllUnsent()
            .sortedWith(compareBy(Message::timestamp))
        if (temporaryMessages.isNotEmpty()) {
            val connectionManager = factory.create(currentServer)
            val client = connectionManager.client
            temporaryMessages.forEach { message ->
                client.sendMessage(
                    message = message.message,
                    messageId = message.id,
                    roomId = message.roomId,
                    avatar = message.avatar,
                    attachments = message.attachments,
                    alias = message.senderAlias
                )
                messageRepository.save(message.copy(isTemporary = false))
            }
        }
    }

    companion object {
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val RETRY_SEND_MESSAGE_ID = 1
    }
}