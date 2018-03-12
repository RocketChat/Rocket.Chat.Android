package chat.rocket.android.widget.emoji

interface EmojiReactionListener {
    /**
     * Callback when an emoji is picked in respect to message by the given id.
     *
     * @param messageId The id of the message being reacted.
     * @param emoji The emoji used to react.
     */
    fun onEmojiReactionAdded(messageId: String, emoji: Emoji)
}