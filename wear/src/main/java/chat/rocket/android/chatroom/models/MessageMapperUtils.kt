package chat.rocket.android.chatroom.models

import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.util.DateTimeHelper
import chat.rocket.android.util.avatarUrl
import chat.rocket.android.util.isNotNullNorEmpty
import chat.rocket.core.model.Message
import chat.rocket.core.model.isSystemMessage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

@PerFragment
class MessageMapperUtils @Inject constructor(
    serverInteractor: GetCurrentServerInteractor
) {
    private val currentServer = serverInteractor.get()
    private val baseUrl = currentServer

    suspend fun map(messages: List<Message>): List<MessageUiModel> = withContext(CommonPool) {
        val list = ArrayList<MessageUiModel>(messages.size)
        messages.forEach {
            list.addAll(
                translate(it)
            )
        }
        return@withContext list
    }

    //not showing the system messages for the time being
    private suspend fun translate(message: Message): List<MessageUiModel> =
        withContext(CommonPool) {
            val list = ArrayList<MessageUiModel>()
            if (!message.isSystemMessage()) {
                mapMessage(message).let {
                    list.add(it)
                }
            }
            return@withContext list
        }

    private suspend fun mapMessage(message: Message): MessageUiModel = withContext(CommonPool) {
        val sender = getSenderName(message)
        val time = getTime(message.timestamp)
        val avatar = getUserAvatar(message)
        val hasAttachments = (message.attachments != null)
        val hasUrls = (message.urls != null)

        //presently messages with attatchments are not supported
        //TODO add support for url's (without the preview)
        val hasUnsupportedMessageFormat = (hasAttachments || hasUrls)

        //TODO implement this later on. This will parse messages and show mentions, emojis instead of plain characters
        //val content = getContent(stripMessageQuotes(message))

        //There is no support for reacting with emojis presently
        MessageUiModel(
            message = message,
            rawData = message,
            messageId = message.id,
            avatar = avatar!!,
            time = time,
            senderName = sender,
            content = message.toString(),
            isPinned = message.pinned,
            attachments = hasUnsupportedMessageFormat
        )
    }

    fun getSenderName(message: Message): CharSequence {
        val username = message.sender?.username
        message.senderAlias.isNotNullNorEmpty { alias ->
            return buildSpannedString {
                append(alias)
                username?.let {
                    append(" ")
                    scale(0.8f) {
                        append("@$username")
                    }
                }
            }
        }
        return username.toString()
    }

    fun getTime(timestamp: Long) =
        DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(timestamp))

    private fun getUserAvatar(message: Message): String? {
        message.avatar?.let {
            return it // Always give preference for overridden avatar from message
        }

        val username = message.sender?.username ?: "?"
        return baseUrl?.let {
            baseUrl.avatarUrl(username)
        }
    }
}