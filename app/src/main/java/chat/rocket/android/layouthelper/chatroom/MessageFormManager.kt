package chat.rocket.android.layouthelper.chatroom

import chat.rocket.android.widget.message.MessageFormLayout

class MessageFormManager(private val messageFormLayout: MessageFormLayout, val callback: MessageFormLayout.ExtraActionSelectionClickListener) {
    private var sendMessageCallback: SendMessageCallback? = null

    init {
        messageFormLayout.setExtraActionSelectionClickListener(callback)
        messageFormLayout.setSubmitTextListener(this::sendMessage)
    }

    fun setSendMessageCallback(sendMessageCallback: SendMessageCallback) {
        this.sendMessageCallback = sendMessageCallback
    }

    fun onMessageSend() {
        clearComposingText()
    }

    fun setEditMessage(message: String) {
        clearComposingText()
        messageFormLayout.setText(message)
    }

    fun clearComposingText() {
        messageFormLayout.setText("")
    }

    fun enableComposingText(enable: Boolean) {
        messageFormLayout.isEnabled = enable
    }

    private fun sendMessage(message: String) {
        sendMessageCallback?.onSubmitText(message)
    }

    interface SendMessageCallback {
        fun onSubmitText(messageText: String)
    }
}