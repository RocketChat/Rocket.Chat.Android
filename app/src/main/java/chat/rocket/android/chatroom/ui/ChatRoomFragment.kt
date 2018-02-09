package chat.rocket.android.chatroom.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisible
import chat.rocket.android.util.showToast
import chat.rocket.android.util.textContent
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
    @Inject lateinit var parser: MessageParser
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var isChatRoomReadOnly: Boolean = false
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var actionSnackbar: ActionSnackbar
    private var citation: String? = null
    private var editingMessageId: String? = null

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
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_chat_room)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadMessages(chatRoomId, chatRoomType)
        setupComposer()
        setupActionSnackbar()
    }

    override fun onDestroyView() {
        presenter.unsubscribeMessages()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chatroom_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pinned_messages -> {
                val intent = Intent(activity, PinnedMessagesActivity::class.java).apply {
                    putExtra(BUNDLE_CHAT_ROOM_ID, chatRoomId)
                    putExtra(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
                    putExtra(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
                }
                startActivity(intent)
            }
        }
        return true
    }

    override fun showMessages(dataSet: List<MessageViewModel>) {
        activity?.apply {
            if (recycler_view.adapter == null) {
                adapter = ChatRoomAdapter(chatRoomType, chatRoomName, presenter)
                recycler_view.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                recycler_view.layoutManager = linearLayoutManager
                recycler_view.itemAnimator = DefaultItemAnimator()
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                            presenter.loadMessages(chatRoomId, chatRoomType, page * 30L)
                        }
                    })
                }
            }

            adapter.addDataSet(dataSet)
        }
    }

    override fun sendMessage(text: String) {
        if (!text.isBlank()) {
            presenter.sendMessage(chatRoomId, text, editingMessageId)
        }
    }

    override fun showNewMessage(message: MessageViewModel) {
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

    override fun dispatchUpdateMessage(index: Int, message: MessageViewModel) {
        adapter.updateItem(message)
    }

    override fun dispatchDeleteMessage(msgId: String) {
        adapter.removeItem(msgId)
    }

    override fun showReplyingAction(username: String, replyMarkdown: String, quotedMessage: String) {
        activity?.apply {
            citation = replyMarkdown
            actionSnackbar.title = username
            actionSnackbar.text = quotedMessage
            actionSnackbar.show()
        }
    }

    override fun showLoading() = view_loading.setVisible(true)

    override fun hideLoading() = view_loading.setVisible(false)

    override fun showMessage(message: String) = showToast(message)

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun copyToClipboard(message: String) {
        activity?.apply {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("", message))
        }
    }

    override fun showEditingAction(roomId: String, messageId: String, text: String) {
        activity?.apply {
            actionSnackbar.title = getString(R.string.action_title_editing)
            actionSnackbar.text = text
            actionSnackbar.show()
            text_message.textContent = text
            editingMessageId = messageId
        }

    }

    private fun setupComposer() {
        if (isChatRoomReadOnly) {
            text_room_is_read_only.setVisible(true)
            top_container.setVisible(false)
        } else {
            text_send.setOnClickListener {
                var textMessage = citation ?: ""
                textMessage = textMessage + text_message.textContent
                sendMessage(textMessage)
                clearActionMessage()
            }
        }
    }

    private fun setupActionSnackbar() {
        actionSnackbar = ActionSnackbar.make(message_list_container, parser = parser)
        actionSnackbar.cancelView.setOnClickListener({
            clearActionMessage()
        })
    }

    private fun clearActionMessage() {
        citation = null
        editingMessageId = null
        text_message.text.clear()
        actionSnackbar.dismiss()
    }
}