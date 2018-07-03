package chat.rocket.android.db

import chat.rocket.android.infrastructure.MessagesRepository
import chat.rocket.core.model.Message

class DatabaseMessagesRepository(val dbManager: DatabaseManager) : MessagesRepository {

    override fun saveMessage(message: Message) {
        saveMessages(listOf(message))
    }

    override fun saveMessages(messages: List<Message>) {
        messages.forEach { message ->

        }
    }

}