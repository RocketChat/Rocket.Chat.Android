package chat.rocket.android.chatroom.service

import android.app.job.JobParameters
import android.app.job.JobService
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
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
                retrySendingMessages(params, currentServer)
                jobFinished(params, false)
            }
        }
        return true
    }

    private suspend fun retrySendingMessages(params: JobParameters?, currentServer: String) {
        val temporaryMessages = messageRepository.getAllUnsent()
            .sortedWith(compareBy(Message::timestamp))
        if (temporaryMessages.isNotEmpty()) {
            val connectionManager = factory.create(currentServer)
            val client = connectionManager.client
            temporaryMessages.forEach { message ->
                try {
                    client.sendMessage(
                        message = message.message,
                        messageId = message.id,
                        roomId = message.roomId,
                        avatar = message.avatar,
                        attachments = message.attachments,
                        alias = message.senderAlias
                    )
                    messageRepository.save(message.copy(isTemporary = false))
                    Timber.d("Sent scheduled message given by id: ${message.id}")
                } catch (ex: Exception) {
                    Timber.e(ex)
                    // TODO - remove the generic message when we implement :userId:/message subscription
                    if (ex is IllegalStateException) {
                        Timber.e(ex, "Probably a read-only problem...")
                        // TODO: For now we are only going to reschedule when api is fixed.
                        messageRepository.removeById(message.id)
                        jobFinished(params, false)
                    } else {
                        // some other error
                        if (ex.message?.contains("E11000", true) == true) {
                            // XXX: Temporary solution. We need proper error codes from the api.
                            messageRepository.save(message.copy(isTemporary = false))
                        }
                        jobFinished(params, true)
                    }
                }
            }
        }
    }

    companion object {
        const val RETRY_SEND_MESSAGE_ID = 1
    }
}