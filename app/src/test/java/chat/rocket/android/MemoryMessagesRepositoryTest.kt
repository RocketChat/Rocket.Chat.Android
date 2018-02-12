package chat.rocket.android

import chat.rocket.android.server.infraestructure.MemoryMessagesRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class MemoryMessagesRepositoryTest {

    val repository = MemoryMessagesRepository()

    val msg = Message(
            id = "messageId",
            roomId = "GENERAL",
            message = "Beam me up, Scotty.",
            timestamp = 1511443964815,
            attachments = null,
            sender = null,
            avatar = null,
            channels = null,
            editedAt = null,
            editedBy = null,
            groupable = true,
            mentions = null,
            parseUrls = false,
            senderAlias = null,
            type = MessageType.MessageRemoved(),
            updatedAt = 1511443964815,
            urls = null
    )

    val msg2 = Message(
            id = "messageId2",
            roomId = "sandbox",
            message = "Highly Illogical",
            timestamp = 1511443964818,
            attachments = null,
            sender = null,
            avatar = null,
            channels = null,
            editedAt = null,
            editedBy = null,
            groupable = true,
            mentions = null,
            parseUrls = false,
            senderAlias = null,
            type = MessageType.MessageRemoved(),
            updatedAt = 1511443964818,
            urls = null
    )

    @Before
    fun setup() {
        repository.clear()
    }

    @Test
    fun `save() should save a single message`() {
        assertThat(repository.getAll().size, isEqualTo(0))
        repository.save(msg)
        val allMessages = repository.getAll()
        assertThat(allMessages.size, isEqualTo(1))
        allMessages[0].apply {
            assertThat(id, isEqualTo("messageId"))
            assertThat(message, isEqualTo("Beam me up, Scotty."))
            assertThat(roomId, isEqualTo("GENERAL"))
        }
    }

    @Test
    fun `saveAll() should all saved messages`() {
        assertThat(repository.getAll().size, isEqualTo(0))
        repository.saveAll(listOf(msg, msg2))
        val allMessages = repository.getAll()
        assertThat(allMessages.size, isEqualTo(2))
        allMessages[0].apply {
            assertThat(id, isEqualTo("messageId"))
            assertThat(message, isEqualTo("Beam me up, Scotty."))
            assertThat(roomId, isEqualTo("GENERAL"))
        }

        allMessages[1].apply {
            assertThat(id, isEqualTo("messageId2"))
            assertThat(message, isEqualTo("Highly Illogical"))
            assertThat(roomId, isEqualTo("sandbox"))
        }
    }

    @Test
    fun `getById() should return a single message`() {
        repository.saveAll(listOf(msg, msg2))
        var singleMsg = repository.getById("messageId")
        assertThat(singleMsg, notNullValue())
        singleMsg!!.apply {
            assertThat(id, isEqualTo("messageId"))
            assertThat(message, isEqualTo("Beam me up, Scotty."))
            assertThat(roomId, isEqualTo("GENERAL"))
        }

        singleMsg = repository.getById("messageId2")
        assertThat(singleMsg, notNullValue())
        singleMsg!!.apply {
            assertThat(id, isEqualTo("messageId2"))
            assertThat(message, isEqualTo("Highly Illogical"))
            assertThat(roomId, isEqualTo("sandbox"))
        }
    }

    @Test
    fun `getByRoomId() should return all messages for room id or an empty list`() {
        repository.saveAll(listOf(msg, msg2))
        var roomMessages = repository.getByRoomId("faAad32fkasods2")
        assertThat(roomMessages.isEmpty(), isEqualTo(true))

        roomMessages = repository.getByRoomId("sandbox")
        assertThat(roomMessages.size, isEqualTo(1))
        roomMessages[0].apply {
            assertThat(id, isEqualTo("messageId2"))
            assertThat(message, isEqualTo("Highly Illogical"))
            assertThat(roomId, isEqualTo("sandbox"))
        }

        roomMessages = repository.getByRoomId("GENERAL")
        assertThat(roomMessages.size, isEqualTo(1))
        roomMessages[0].apply {
            assertThat(id, isEqualTo("messageId"))
            assertThat(message, isEqualTo("Beam me up, Scotty."))
            assertThat(roomId, isEqualTo("GENERAL"))
        }
    }
}