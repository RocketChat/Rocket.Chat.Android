package chat.rocket.android.server.infraestructure

import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.Operation
import chat.rocket.android.db.model.MessagesSync
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.util.retryDB
import chat.rocket.core.model.Message
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class DatabaseMessagesRepository(
    private val dbManager: DatabaseManager,
    private val mapper: DatabaseMessageMapper
) : MessagesRepository {

    override suspend fun getById(id: String): Message? = withContext(CommonPool) {
        retryDB("getMessageById($id)") {
            dbManager.messageDao().getMessageById(id)?.let { message -> mapper.map(message) }
        }
    }

    override suspend fun getByRoomId(roomId: String): List<Message> = withContext(CommonPool) {
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

    override suspend fun getRecentMessages(roomId: String, count: Long): List<Message> = withContext(CommonPool) {
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

    override suspend fun saveAll(messages: List<Message>) {
        dbManager.processMessagesBatch(messages).join()
    }

    override suspend fun removeById(id: String) {
        withContext(CommonPool) {
            retryDB("delete($id)") { dbManager.messageDao().delete(id) }
        }
    }

    override suspend fun removeByRoomId(roomId: String) {
        withContext(CommonPool) {
            retryDB("deleteByRoomId($roomId)") {
                dbManager.messageDao().deleteByRoomId(roomId)
            }
        }
    }

    override suspend fun getAllUnsent(): List<Message> = withContext(CommonPool) {
        retryDB("getUnsentMessages") {
            dbManager.messageDao().getUnsentMessages()
                    .distinctBy { it.message.message.id }
                    .let { mapper.map(it) }
        }
    }

    override suspend fun saveLastSyncDate(roomId: String, timeMillis: Long) {
        dbManager.sendOperation(Operation.SaveLastSync(MessagesSync(roomId, timeMillis)))
    }

    override suspend fun getLastSyncDate(roomId: String): Long? = withContext(CommonPool) {
        retryDB("getLastSync($roomId)") {
            dbManager.messageDao().getLastSync(roomId)?.let { it.timestamp }
        }
    }
}