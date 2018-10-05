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
     * @param roomId The room id.
     * @return A list of Message objects for the room with given room id or an empty list.
     */
    suspend fun getByRoomId(roomId: String): List<Message>

    /**
     * Get most recent messages up to count different users.
     *
     * @param roomId The id of the room the messages are.
     * @param count The count last messages to get.
     *
     * @return List of last count messages.
     */
    suspend fun getRecentMessages(roomId: String, count: Long): List<Message>

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
     * Remove message by id.
     *
     * @param id The id of the message to remove.
     */
    suspend fun removeById(id: String)

    /**
     * Remove all messages from a given room.
     *
     * @param roomId The room id where messages are to be removed.
     */
    suspend fun removeByRoomId(roomId: String)

    suspend fun getAllUnsent(): List<Message>

    /**
     * Save time of the latest room messages sync.
     * Call this fun only when the latest messages list being received via /history or /messages network calls
     *
     * @param roomId The id of the room the messages are.
     * @param timeMillis time of room messages sync or the latest room message timestamp(which came with /history request)
     */
    suspend fun saveLastSyncDate(roomId: String, timeMillis: Long)

    /**
     * Get time when the room chat history has been loaded last time.
     *
     * @param roomId The id of the room the messages are.
     *
     * @return Last Sync time or Null.
     */
    suspend fun getLastSyncDate(roomId: String): Long?
}