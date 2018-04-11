package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.model.Message
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class SharedPreferencesMessagesRepository(
        private val prefs: SharedPreferences,
        private val moshi: Moshi)
    : MessagesRepository {

    override suspend fun getById(id: String): Message? = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        if (prefs.all.values.isEmpty()) {
            return@withContext null
        }
        val values = prefs.all.values as Collection<String>
        return@withContext values.map { adapter.fromJson(it) }.firstOrNull { it?.id == id }
    }

    override suspend fun getByRoomId(rid: String): List<Message> = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        if (prefs.all.values.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        val values = prefs.all.values as Collection<String>
        return@withContext values.mapNotNull { adapter.fromJson(it) }.filter {
            it.roomId == rid
        }.toList().sortedWith(compareBy(Message::timestamp)).reversed()
    }

    override suspend fun getRecentMessages(rid: String, count: Long): List<Message> {
        return getByRoomId(rid).sortedByDescending { it.timestamp }
                .distinctBy { it.sender }.take(count.toInt())
    }

    override suspend fun getAll(): List<Message> = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        if (prefs.all.values.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        val values = prefs.all.values as Collection<String>
        return@withContext values.mapNotNull { adapter.fromJson(it) }
    }

    override suspend fun getAllUnsent(): List<Message> = withContext(CommonPool) {
        if (prefs.all.values.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        val all = prefs.all.values as Collection<String>
        val adapter = moshi.adapter<Message>(Message::class.java)
        return@withContext all.mapNotNull { adapter.fromJson(it) }
                .filter { it.isTemporary ?: false }
    }

    override suspend fun getUnsentByRoomId(roomId: String): List<Message> = withContext(CommonPool) {
        val allByRoomId = getByRoomId(roomId)
        if (allByRoomId.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        return@withContext allByRoomId.filter { it.isTemporary ?: false }
    }

    override suspend fun save(message: Message) = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        prefs.edit().putString(message.id, adapter.toJson(message)).apply()
    }

    override suspend fun saveAll(newMessages: List<Message>) = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        val editor = prefs.edit()
        for (msg in newMessages) {
            editor.putString(msg.id, adapter.toJson(msg))
        }
        editor.apply()
    }

    override suspend fun clear() = withContext(CommonPool) {
        prefs.edit().clear().apply()
    }

    override suspend fun removeById(id: String) = withContext(CommonPool) {
        prefs.edit().putString(id, null).apply()
    }

    override suspend fun removeByRoomId(rid: String) {
        withContext(CommonPool) {
            val adapter = moshi.adapter<Message>(Message::class.java)
            val editor = prefs.edit()
            prefs.all.entries.forEach {
                val value = it.value
                if (value is String) {
                    val message = adapter.fromJson(value)
                    if (message?.roomId == rid) {
                        editor.putString(message.id, null)
                    }
                }
            }
            editor.apply()
        }
    }
}