package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.model.Message
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class MemoryMessagesRepository : MessagesRepository {

    private val messages: HashMap<String, Message> = HashMap()

    override suspend fun getById(id: String): Message? = withContext(CommonPool) {
        return@withContext messages[id]
    }

    override suspend fun getByRoomId(rid: String): List<Message> = withContext(CommonPool) {
        return@withContext messages.filter { it.value.roomId == rid }.values.toList()
    }

    override suspend fun getRecentMessages(rid: String, count: Long): List<Message> = withContext(CommonPool) {
        return@withContext getByRoomId(rid).sortedByDescending { it.timestamp }
                .distinctBy { it.sender }.take(count.toInt())
    }

    override suspend fun getAll(): List<Message> = withContext(CommonPool) {
        return@withContext messages.values.toList()
    }

    override suspend fun getUnsentByRoomId(roomId: String): List<Message> = withContext(CommonPool) {
        val allByRoomId = getByRoomId(roomId)
        if (allByRoomId.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        return@withContext allByRoomId.filter { it.isTemporary ?: false && it.roomId == roomId }
    }

    override suspend fun getAllUnsent(): List<Message> = withContext(CommonPool) {
        val all = getAll()
        if (all.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        return@withContext all.filter { it.isTemporary ?: false }
    }

    override suspend fun save(message: Message) = withContext(CommonPool) {
        messages[message.id] = message
    }

    override suspend fun saveAll(newMessages: List<Message>) = withContext(CommonPool) {
        for (msg in newMessages) {
            messages[msg.id] = msg
        }
    }

    override suspend fun clear() = withContext(CommonPool) {
        messages.clear()
    }

    override suspend fun removeById(id: String) {
        withContext(CommonPool) {
            messages.remove(id)
        }
    }

    override suspend fun removeByRoomId(rid: String) = withContext(CommonPool) {
        val roomMessages = messages.filter { it.value.roomId == rid }.values
        roomMessages.forEach {
            messages.remove(it.roomId)
        }
    }
}