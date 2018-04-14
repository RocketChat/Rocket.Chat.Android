package chat.rocket.android.server.domain

import chat.rocket.core.model.Message

interface MessagesRepository {

    /**
     * Get message by its message id.
     *
     * @param id The id of the message to get.
     *
     * @return The Message object given by the id or null if message wasn't found.
     */
    suspend fun getById(id: String): Message?

    /**
     * Get all messages from the current room id.
     *
     * @param rid The room id.
     * @return A list of Message objects for the room with given room id or an empty list.
     */
    suspend fun getByRoomId(rid: String): List<Message>

    /**
     * Get most recent messages up to count different users.
     *
     * @param rid The id of the room the messages are.
     * @param count The count last messages to get.
     *
     * @return List of last count messages.
     */
    suspend fun getRecentMessages(rid: String, count: Long): List<Message>

    /**
     * Get all messages. Use carefully!
     *
     * @return All messages or an empty list.
     */
    suspend fun getAll(): List<Message>

    /**
     * Save a single message object.
     *
     * @param The message object to saveAll.
     */
    suspend fun save(message: Message)

    /**
     * Save a list of messages.
     */
    suspend fun saveAll(newMessages: List<Message>)

    /**
     * Removes all messages.
     */
    suspend fun clear()

    /**
     * Remove message by id.
     *
     * @param id The id of the message to remove.
     */
    suspend fun removeById(id: String)

    /**
     * Remove all messages from a given room.
     *
     * @param rid The room id where messages are to be removed.
     */
    suspend fun removeByRoomId(rid: String)

    suspend fun getAllUnsent(): List<Message>

    suspend fun getUnsentByRoomId(roomId: String): List<Message>
}