package chat.rocket.android.chatroom.viewmodel

import android.content.Context
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.TokenRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class MessageViewModelMapper @Inject constructor(private val context: Context,
                                                 private val tokenRepository: TokenRepository,
                                                 private val messageParser: MessageParser,
                                                 private val messagesRepository: MessagesRepository) {

    suspend fun mapToViewModel(message: Message, settings: Map<String, Value<Any>>) = launch(CommonPool) {
        MessageViewModel(this@MessageViewModelMapper.context, tokenRepository.get(), message, settings, messageParser, messagesRepository)
    }

    suspend fun mapToViewModelList(messageList: List<Message>, settings: Map<String, Value<Any>>): List<MessageViewModel> {
        return messageList.map { MessageViewModel(context, tokenRepository.get(), it, settings, messageParser, messagesRepository) }
    }
}