package chat.rocket.android.chatroom.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_chat_room.*
import kotlinx.android.synthetic.main.message_composer.*
import javax.inject.Inject

fun newInstance(chatRoomId: String, chatRoomName: String, chatRoomType: String, isChatRoomReadOnly: Boolean): Fragment {
    return ChatRoomFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
            putBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"
private const val BUNDLE_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"

class ChatRoomFragment : Fragment(), ChatRoomView {

    @Inject lateinit var presenter: ChatRoomPresenter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var isChatRoomReadOnly: Boolean = false
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomName = bundle.getString(BUNDLE_CHAT_ROOM_NAME)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
            isChatRoomReadOnly = bundle.getBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_chat_room)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadMessages(chatRoomId, chatRoomType)
        presenter.subscribeMessages(chatRoomId)
        setupComposer()
    }

    override fun onDestroyView() {
        presenter.unsubscribeMessages()
        super.onDestroyView()
    }

    override fun showMessages(dataSet: List<Message>, serverUrl: String, settings: Map<String, Value<Any>>?) {
        activity?.apply {
            if (recycler_view.adapter == null) {
                adapter = ChatRoomAdapter(this, serverUrl, settings)
                recycler_view.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                recycler_view.layoutManager = linearLayoutManager
                recycler_view.itemAnimator = DefaultItemAnimator()
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                            presenter.loadMessages(chatRoomId, chatRoomType, page * 30)
                        }
                    })
                }
            }

            adapter.addDataSet(dataSet)
        }
    }

    override fun sendMessage(text: String) {
        if (!text.isBlank()) {
            presenter.sendMessage(chatRoomId, text)
        }
    }

    override fun showNewMessage(message: Message) {
        text_message.textContent = ""
        adapter.addItem(message)
        recycler_view.smoothScrollToPosition(0)
    }

    override fun disableMessageInput() {
        text_send.isEnabled = false
        text_message.isEnabled = false
    }

    override fun enableMessageInput(clear: Boolean) {
        text_send.isEnabled = true
        text_message.isEnabled = true
        if (clear) text_message.textContent = ""
    }

    override fun dispatchUpdateMessage(index: Int, message: Message) {
        adapter.updateItem(index, message)
    }

    override fun showLoading() = view_loading.setVisibility(true)

    override fun hideLoading() = view_loading.setVisibility(false)

    override fun showMessage(message: String) = Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun setupComposer() {
        if (isChatRoomReadOnly) {
            text_room_is_read_only.setVisibility(true)
            top_container.setVisibility(false)
        } else {
            text_send.setOnClickListener { sendMessage(text_message.textContent) }
        }
    }
}