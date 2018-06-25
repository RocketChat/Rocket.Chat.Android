package chat.rocket.android.emoji

interface EmojiReactionListener {
    /**
     * Callback when an emoji is picked in respect to message by the given id.
     *
     * @param messageId The id of the message being reacted.
     * @param emoji The emoji used to react.
     */
    fun onReactionAdded(messageId: String, emoji: Emoji)

    /**
     * Callback when an added reaction is touched.
     *
     * @param messageId The id of the message with the reaction.
     * @param emojiShortname The shortname of the emoji (:grin:, :smiley:, etc).
     */
    fun onReactionTouched(messageId: String, emojiShortname: String)
}