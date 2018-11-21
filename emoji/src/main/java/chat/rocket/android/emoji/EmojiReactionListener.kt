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

    /**
     * Callback when an added reaction is long-clicked.
     *
     * @param shortname The shortname of the emoji (:grin:, :smiley:, etc).
     * @param isCustom Whether the reaction is custom or one of the defaults.
     * @param url In case of a custom emoji, this is the url to find it. Can be null if not a custom.
     * @param usernames The list of usernames of users who added the reaction.
     */
    fun onReactionLongClicked(shortname: String, isCustom: Boolean, url: String?, usernames: List<String>)
}
