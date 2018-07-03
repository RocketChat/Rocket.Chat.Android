package chat.rocket.android.infrastructure

import chat.rocket.core.model.Message

interface MessagesRepository {
    fun saveMessage(message: Message)
    fun saveMessages(messages: List<Message>)
}