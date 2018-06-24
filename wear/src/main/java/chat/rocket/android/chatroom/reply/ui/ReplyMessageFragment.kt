package chat.rocket.android.chatroom.reply.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.reply.presentation.ReplyMessagePresenter
import chat.rocket.android.chatroom.reply.presentation.ReplyMessageView
import chat.rocket.android.util.showToast
import chat.rocket.android.util.ui
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_reply_message.*
import javax.inject.Inject

fun newInstance(chatRoomId: String): Fragment {
    return ReplyMessageFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
        }
    }

}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class ReplyMessageFragment : Fragment(), ReplyMessageView {

    @Inject
    lateinit var presenter: ReplyMessagePresenter
    @Inject
    lateinit var navigator: ChatRoomNavigator
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        getRoomIdFromBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_reply_message, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(action_message_send) {
            setOnClickListener {
                if (reply_message_edit_text.text.isNotBlank()) {
                    presenter.sendMessage(chatRoomId, reply_message_edit_text.text.toString())
                }
            }
        }
    }

    override fun messageSentSuccessfully() {
        hideLoading()
        navigator.removeReplyMessageFragment()
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() =
        showMessage(getString(R.string.msg_generic_error))

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    private fun getRoomIdFromBundle() {
        val bundle = arguments
        if (arguments != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
        }
    }
}