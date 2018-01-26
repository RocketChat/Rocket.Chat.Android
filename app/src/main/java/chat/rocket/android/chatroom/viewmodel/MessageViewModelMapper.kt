package chat.rocket.android.chatroom.viewmodel

import android.content.Context
import chat.rocket.core.TokenRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import javax.inject.Inject

class MessageViewModelMapper @Inject constructor(private val context: Context, private val tokenRepository: TokenRepository) {

    suspend fun mapToViewModel(message: Message, settings: Map<String, Value<Any>>?) = MessageViewModel(context, tokenRepository.get(), message, settings)

    suspend fun mapToViewModelList(messageList: List<Message>, settings: Map<String, Value<Any>>?): List<MessageViewModel> {
        return messageList.map { MessageViewModel(context, tokenRepository.get(), it, settings) }
    }
}