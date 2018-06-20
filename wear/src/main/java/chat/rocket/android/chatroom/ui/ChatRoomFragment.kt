package chat.rocket.android.chatroom.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import chat.rocket.android.R
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.util.showToast
import chat.rocket.android.util.ui
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_chat_room.*
import javax.inject.Inject

fun newInstance(
    chatRoomId: String,
    chatRoomName: String,
    chatRoomType: String
): Fragment {
    return ChatRoomFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"

class ChatRoomFragment : Fragment(), ChatRoomView {

    @Inject
    lateinit var presenter: ChatRoomPresenter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        getValuesFromBundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_chat_room, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setUpClickListeners()
        presenter.loadAndShowMessages(chatRoomId, chatRoomType)
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showLoading() {
        view_loading.isVisible = true
        reply_button_view.isVisible = false
    }

    override fun hideLoading() {
        view_loading.isVisible = false
        reply_button_view.isVisible = true
    }

    override fun showMessages(dataSet: List<MessageUiModel>) {
        adapter = ChatRoomAdapter(dataSet)
        ui {
            chat_list.adapter = adapter
        }
    }

    private fun getValuesFromBundle() {
        val bundle = arguments
        if (arguments != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomName = bundle.getString(BUNDLE_CHAT_ROOM_NAME)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
        }
    }

    private fun setUpRecyclerView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        chat_list.layoutManager = layoutManager
        chat_list.itemAnimator = DefaultItemAnimator()
        chat_list.isCircularScrollingGestureEnabled = true
        chat_list.scrollDegreesPerScreen = 90f
    }

    private fun setUpClickListeners() {
        reply_button_view.setOnClickListener {
            //open view to write message and send it
        }
    }
}