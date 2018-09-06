package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.core.model.Message
import com.squareup.moshi.Moshi
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class SharedPreferencesMessagesRepository(
    private val prefs: SharedPreferences,
    private val moshi: Moshi,
    private val currentServerInteractor: GetCurrentServerInteractor
) : MessagesRepository {
    private val KEY_LAST_SYNC_DATE = "KEY_LAST_SYNC_DATE"

    override suspend fun saveLastSyncDate(rid: String, timeMillis: Long) {
        withContext(CommonPool) {
            currentServerInteractor.get()?.let {
                prefs.edit().putLong(getSyncDateKey(it, rid), timeMillis).apply()
            }
        }
    }

    override suspend fun getLastSyncDate(rid: String): Long? = withContext(CommonPool) {
        currentServerInteractor.get()?.also { server ->
            if (!prefs.contains(getSyncDateKey(server, rid)))
                return@withContext null
            val time = prefs.getLong(getSyncDateKey(server, rid), -1)
            return@withContext if (time == -1L) null else time
        }
        return@withContext null
    }

    private fun getSyncDateKey(server: String, rid: String) = "${KEY_LAST_SYNC_DATE}_$server $rid"

    override suspend fun getById(id: String): Message? = withContext(CommonPool) {
        currentServerInteractor.get()?.also { server ->
            if (prefs.all.values.isEmpty()) {
                return@withContext null
            }
            val adapter = moshi.adapter<Message>(Message::class.java)
            val values = prefs.all.entries.filter { it.key.startsWith(server) }
                .map { it.value } as Collection<String>
            return@withContext values.map { adapter.fromJson(it) }.firstOrNull { it?.id == id }
        }
        return@withContext null
    }

    override suspend fun getByRoomId(rid: String): List<Message> = withContext(CommonPool) {
        currentServerInteractor.get()?.also { server ->
            val adapter = moshi.adapter<Message>(Message::class.java)
            if (prefs.all.values.isEmpty()) {
                return@withContext emptyList<Message>()
            }
            val values = prefs.all.entries.filter { it.key.startsWith(server) }
                .map { it.value } as Collection<String>
            return@withContext values.mapNotNull { adapter.fromJson(it) }.filter {
                it.roomId == rid
            }.toList().sortedWith(compareBy(Message::timestamp)).reversed()
        }
        return@withContext emptyList<Message>()
    }

    override suspend fun getRecentMessages(rid: String, count: Long): List<Message> = withContext(CommonPool) {
        return@withContext getByRoomId(rid).sortedByDescending { it.timestamp }
            .distinctBy { it.sender }.take(count.toInt())
    }

    override suspend fun getAll(): List<Message> = withContext(CommonPool) {
        val adapter = moshi.adapter<Message>(Message::class.java)
        if (prefs.all.values.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        currentServerInteractor.get()?.also { server ->
            val values = prefs.all.entries.filter { it.key.startsWith(server) }
                .map { it.value } as Collection<String>
            return@withContext values.mapNotNull { adapter.fromJson(it) }
        }
        return@withContext emptyList<Message>()
    }

    override suspend fun getAllUnsent(): List<Message> = withContext(CommonPool) {
        if (prefs.all.values.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        currentServerInteractor.get()?.also { server ->
            val values = prefs.all.entries.filter { it.key.startsWith(server) }
                .map { it.value } as Collection<String>
            val adapter = moshi.adapter<Message>(Message::class.java)
            return@withContext values.mapNotNull { adapter.fromJson(it) }
                .filter { it.isTemporary ?: false }
        }
        return@withContext emptyList<Message>()
    }

    override suspend fun getUnsentByRoomId(roomId: String): List<Message> = withContext(CommonPool) {
        val allByRoomId = getByRoomId(roomId)
        if (allByRoomId.isEmpty()) {
            return@withContext emptyList<Message>()
        }
        return@withContext allByRoomId.filter { it.isTemporary ?: false }
    }

    override suspend fun save(message: Message) {
        withContext(CommonPool) {
            currentServerInteractor.get()?.also {
                val adapter = moshi.adapter<Message>(Message::class.java)
                prefs.edit().putString("${it}_${message.id}", adapter.toJson(message)).apply()
            }
        }
    }

    override suspend fun saveAll(newMessages: List<Message>) {
        withContext(CommonPool) {
            currentServerInteractor.get()?.also {
                val adapter = moshi.adapter<Message>(Message::class.java)
                val editor = prefs.edit()
                for (msg in newMessages) {
                    editor.putString("${it}_${msg.id}", adapter.toJson(msg))
                }
                editor.apply()
            }
        }
    }

    override suspend fun clear() = withContext(CommonPool) {
        prefs.edit().clear().apply()
    }

    override suspend fun removeById(id: String) {
        withContext(CommonPool) {
            currentServerInteractor.get()?.also {
                prefs.edit().putString("${it}_$id", null).apply()
            }
        }
    }

    override suspend fun removeByRoomId(rid: String) {
        withContext(CommonPool) {
            currentServerInteractor.get()?.also { server ->
                val adapter = moshi.adapter<Message>(Message::class.java)
                val editor = prefs.edit()
                prefs.all.entries.forEach {
                    val value = it.value
                    if (value is String) {
                        val message = adapter.fromJson(value)
                        if (message?.roomId == rid) {
                            editor.putString("${server}_${message.id}", null)
                        }
                    }
                }
                editor.apply()
            }
        }
    }
}