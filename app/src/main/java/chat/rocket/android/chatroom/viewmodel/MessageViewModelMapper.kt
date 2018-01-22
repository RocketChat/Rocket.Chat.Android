package chat.rocket.android.chatroom.viewmodel

import chat.rocket.core.model.Message
import chat.rocket.core.model.Value

object MessageViewModelMapper {

    suspend fun mapToViewModel(message: Message, settings: Map<String, Value<Any>>?) = MessageViewModel(message, settings)

    suspend fun mapToViewModelList(messageList: List<Message>, settings: Map<String, Value<Any>>?): List<MessageViewModel> {
        val vmList = mutableListOf<MessageViewModel>()
        for (msg in messageList) {
            vmList.add(MessageViewModel(msg, settings))
        }
        return vmList
    }
}