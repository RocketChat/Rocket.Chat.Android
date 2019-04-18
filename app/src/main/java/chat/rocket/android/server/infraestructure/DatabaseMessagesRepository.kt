package chat.rocket.android.server.infraestructure

import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.Operation
import chat.rocket.android.db.model.MessagesSync
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.util.retryDB
import chat.rocket.core.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseMessagesRepository(
    private val dbManager: DatabaseManager,
    private val mapper: DatabaseMessageMapper
) : MessagesRepository {

    override suspend fun getById(id: String): Message? = withContext(Dispatchers.IO) {
        retryDB("getMessageById($id)") {
            dbManager.messageDao().getMessageById(id)?.let { message -> mapper.map(message) }
        }
    }

    override suspend fun getByRoomId(roomId: String): List<Message> = withContext(Dispatchers.IO) {
        // FIXME - investigate how to avoid this distinctBy here, since DAO is returning a lot of
        // duplicate rows (something related to our JOINS and relations on Room)
        retryDB("getMessagesByRoomId($roomId)") {
            dbManager.messageDao().getMessagesByRoomId(roomId)
                .distinctBy { it.message.message.id }
                .let { messages ->
                    mapper.map(messages)
                }
        }
    }

    override suspend fun getRecentMessages(roomId: String, count: Long): List<Message> =
        withContext(Dispatchers.IO) {
            retryDB("getRecentMessagesByRoomId($roomId, $count)") {
                dbManager.messageDao().getRecentMessagesByRoomId(roomId, count)
                    .distinctBy { it.message.message.id }
                    .let { messages ->
                        mapper.map(messages)
                    }
            }
        }

    override suspend fun save(message: Message) {
        dbManager.processMessagesBatch(listOf(message)).join()
    }

    override suspend fun saveAll(newMessages: List<Message>) {
        dbManager.processMessagesBatch(newMessages).join()
    }

    override suspend fun removeById(id: String) {
        withContext(Dispatchers.IO) {
            retryDB("delete($id)") { dbManager.messageDao().delete(id) }
        }
    }

    override suspend fun removeByRoomId(roomId: String) {
        withContext(Dispatchers.IO) {
            retryDB("deleteByRoomId($roomId)") {
                dbManager.messageDao().deleteByRoomId(roomId)
            }
        }
    }

    override suspend fun getAllUnsent(): List<Message> = withContext(Dispatchers.IO) {
        retryDB("getUnsentMessages") {
            dbManager.messageDao().getUnsentMessages()
                .distinctBy { it.message.message.id }
                .let { mapper.map(it) }
        }
    }

    override suspend fun saveLastSyncDate(roomId: String, timeMillis: Long) {
        dbManager.sendOperation(Operation.SaveLastSync(MessagesSync(roomId, timeMillis)))
    }

    override suspend fun getLastSyncDate(roomId: String): Long? = withContext(Dispatchers.IO) {
        retryDB("getLastSync($roomId)") {
            dbManager.messageDao().getLastSync(roomId)?.timestamp
        }
    }
}