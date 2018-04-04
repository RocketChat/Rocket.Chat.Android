package chat.rocket.android.widget.emoji

interface EmojiKeyboardListener {
    /**
     * When an emoji is selected on the picker.
     *
     * @param emoji The selected emoji
     */
    fun onEmojiAdded(emoji: Emoji)

    /**
     * When backspace key is clicked.
     *
     * @param keyCode The key code pressed as defined
     *
     * @see android.view.KeyEvent
     */
    fun onNonEmojiKeyPressed(keyCode: Int)
}