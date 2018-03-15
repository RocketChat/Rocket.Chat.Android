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
    fun getById(id: String): Message?

    /**
     * Get all messages from the current room id.
     *
     * @param rid The room id.
     * @return A list of Message objects for the room with given room id or an empty list.
     */
    fun getByRoomId(rid: String): List<Message>

    /**
     * Get most recent messages up to count different users.
     *
     * @param rid The id of the room the messages are.
     * @param count The count last messages to get.
     *
     * @return List of last count messages.
     */
    fun getRecentMessages(rid: String, count: Long): List<Message>

    /**
     * Get all messages. Use carefully!
     *
     * @return All messages or an empty list.
     */
    fun getAll(): List<Message>

    /**
     * Save a single message object.
     *
     * @param The message object to saveAll.
     */
    fun save(message: Message)

    /**
     * Save a list of messages.
     */
    fun saveAll(newMessages: List<Message>)

    /**
     * Removes all messages.
     */
    fun clear()

    /**
     * Remove message by id.
     *
     * @param id The id of the message to remove.
     */
    fun removeById(id: String)

    /**
     * Remove all messages from a given room.
     *
     * @param rid The room id where messages are to be removed.
     */
    fun removeByRoomId(rid: String)
}