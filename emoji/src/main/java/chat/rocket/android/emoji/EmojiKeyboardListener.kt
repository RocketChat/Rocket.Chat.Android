package chat.rocket.android.emoji

interface EmojiKeyboardListener {
    /**
     * Callback when an emoji is selected on the picker (optional operation).
     *
     * @param emoji The selected emoji
     */
    fun onEmojiAdded(emoji: Emoji) {}

    /**
     * Callback when backspace key is clicked (optional operation).
     *
     * @param keyCode The key code pressed as defined
     *
     * @see android.view.KeyEvent
     */
    fun onNonEmojiKeyPressed(keyCode: Int) {}
}