package chat.rocket.android.layouthelper.chatroom

import android.text.TextUtils
import android.view.View
import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.message.MessageFormLayout
import chat.rocket.core.models.Message

class MessageFormManager(private val messageFormLayout: MessageFormLayout, val callback: MessageFormLayout.ExtraActionSelectionClickListener) {
    private var sendMessageCallback: SendMessageCallback? = null
    private var replyMarkDown: String = ""

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

    fun setReply(absoluteUrl: AbsoluteUrl, replyMarkDown: String, message: Message) {
        this.replyMarkDown = replyMarkDown
        messageFormLayout.setReplyContent(absoluteUrl, message)
        messageFormLayout.setReplyCancelListener({
            this.replyMarkDown = ""
            messageFormLayout.clearReplyContent()
        })
    }

    private fun sendMessage(message: String) {
        val finalMessage = if (replyMarkDown.isNotEmpty()) "$replyMarkDown $message" else message
        replyMarkDown = ""
        sendMessageCallback?.onSubmitText(finalMessage)
    }

    interface SendMessageCallback {
        fun onSubmitText(messageText: String)
    }
}