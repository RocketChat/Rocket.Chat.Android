package chat.rocket.android.chatroom.viewmodel

import android.content.Context
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.TokenRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject

class MessageViewModelMapper @Inject constructor(private val context: Context,
                                                 private val tokenRepository: TokenRepository,
                                                 private val messageParser: MessageParser,
                                                 private val messagesRepository: MessagesRepository,
                                                 private val localRepository: LocalRepository,
                                                 private val currentServerRepository: CurrentServerRepository) {

    suspend fun mapToViewModel(message: Message, settings: Map<String, Value<Any>>): MessageViewModel = withContext(CommonPool) {
        Timber.d("mapping message ${message.id}")
        MessageViewModel(
                this@MessageViewModelMapper.context,
                tokenRepository.get(),
                message,
                settings,
                messageParser,
                messagesRepository,
                localRepository,
                currentServerRepository
        )
    }

    suspend fun mapToViewModelList(messageList: List<Message>, settings: Map<String, Value<Any>>): List<MessageViewModel> {
        return messageList.map { MessageViewModel(context, tokenRepository.get(), it, settings,
                messageParser, messagesRepository, localRepository, currentServerRepository) }
    }
}