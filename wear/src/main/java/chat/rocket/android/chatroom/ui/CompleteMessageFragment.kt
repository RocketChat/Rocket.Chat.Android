package chat.rocket.android.chatroom.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import kotlinx.android.synthetic.main.fragment_complete_message.*

fun newInstance(
    message: MessageUiModel
): Fragment {
    return CompleteMessageFragment().apply {
        arguments = Bundle(1).apply {
            putCharSequence(BUNDLE_SENDER_NAME, message.senderName)
            putCharSequence(BUNDLE_MESSAGE_TIME, message.time)
            putString(BUNDLE_SENDER_AVATAR, message.avatar)
            putCharSequence(BUNDLE_MESSAGE, message.content)
            putBoolean(BUNDLE_HAS_ATTACHMENTS, message.attachments)
        }
    }
}

private const val BUNDLE_SENDER_NAME = "sender_name"
private const val BUNDLE_MESSAGE_TIME = "message_time"
private const val BUNDLE_SENDER_AVATAR = "sender_avatar"
private const val BUNDLE_MESSAGE = "message"
private const val BUNDLE_HAS_ATTACHMENTS = "has_attachments"

class CompleteMessageFragment : Fragment() {

    private lateinit var messageSenderName: CharSequence
    private lateinit var messageTime: CharSequence
    private lateinit var senderAvatar: String
    private lateinit var message: CharSequence
    private var hasAttachments: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getValuesFromBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_complete_message, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCompleteMessage()
    }

    private fun getValuesFromBundle() {
        val bundle = arguments
        if (arguments != null) {
            messageSenderName = bundle.getCharSequence(BUNDLE_SENDER_NAME)
            messageTime = bundle.getCharSequence(BUNDLE_MESSAGE_TIME)
            senderAvatar = bundle.getString(BUNDLE_SENDER_AVATAR)
            message = bundle.getCharSequence(BUNDLE_MESSAGE)
            hasAttachments = bundle.getBoolean(BUNDLE_HAS_ATTACHMENTS)
        }
    }

    private fun showCompleteMessage() {
        complete_message_sender_name.text = messageSenderName
        complete_message_sender_avatar.setImageURI(senderAvatar)
        complete_message.text = message
        if (hasAttachments) {
            complete_message_attachments_msg.isVisible = true
        }
    }
}