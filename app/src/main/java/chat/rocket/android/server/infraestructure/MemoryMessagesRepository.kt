package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.model.Message

class MemoryMessagesRepository : MessagesRepository {

    private val messages: HashMap<String, Message> = HashMap()

    override fun getById(id: String): Message? {
        return messages[id]
    }

    override fun getByRoomId(rid: String): List<Message> {
        return messages.filter { it.value.roomId == rid }.values.toList()
    }

    override fun getRecentMessages(rid: String, count: Long): List<Message> {
        return getByRoomId(rid).sortedByDescending { it.timestamp }
                .distinctBy { it.sender }.take(count.toInt())
    }

    override fun getAll(): List<Message> = messages.values.toList()

    override fun save(message: Message) {
        messages[message.id] = message
    }

    override fun saveAll(newMessages: List<Message>) {
        for (msg in newMessages) {
            messages[msg.id] = msg
        }
    }

    override fun clear() {
        messages.clear()
    }

    override fun removeById(id: String) {
        messages.remove(id)
    }

    override fun removeByRoomId(rid: String) {
        val roomMessages = messages.filter { it.value.roomId == rid }.values
        roomMessages.forEach {
            messages.remove(it.roomId)
        }
    }
}