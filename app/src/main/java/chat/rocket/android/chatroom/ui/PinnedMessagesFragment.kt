package chat.rocket.android.chatroom.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.presentation.PinnedMessagesPresenter
import chat.rocket.android.chatroom.presentation.PinnedMessagesView
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.widget.emoji.Emoji
import chat.rocket.android.widget.emoji.EmojiListenerAdapter
import chat.rocket.android.widget.emoji.EmojiPickerPopup
import chat.rocket.android.widget.emoji.EmojiReactionListener
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_pinned_messages.*
import timber.log.Timber
import javax.inject.Inject

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"

fun newPinnedMessagesFragment(chatRoomId: String, chatRoomType: String, chatRoomName: String): Fragment {
    return PinnedMessagesFragment().apply {
        arguments = Bundle(3).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
        }
    }
}

class PinnedMessagesFragment : Fragment(), PinnedMessagesView, EmojiReactionListener {

    @Inject lateinit var presenter: PinnedMessagesPresenter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomName = bundle.getString(BUNDLE_CHAT_ROOM_NAME)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater.inflate(R.layout.fragment_pinned_messages, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadPinnedMessages(chatRoomId)
    }

    override fun showLoading() = view_loading.setVisible(true)

    override fun hideLoading() = view_loading.setVisible(false)

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun copyToClipboard(message: String) {
        activity?.apply {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("", message)
        }
    }

    override fun dispatchDeleteMessage(msgId: String) {
        adapter.removeItem(msgId)
    }

    override fun showReactionsPopup(messageId: String) {
        context?.let {
            val emojiPickerPopup = EmojiPickerPopup(it)
            emojiPickerPopup.listener = object : EmojiListenerAdapter() {
                override fun onEmojiAdded(emoji: Emoji) {
                    onReactionAdded(messageId, emoji)
                    Timber.e("onEmojiAdded")
                }
            }
            emojiPickerPopup.show()
        }
    }

    override fun onReactionTouched(messageId: String, emojiShortname: String) {
        presenter.react(messageId, emojiShortname)
    }

    override fun onReactionAdded(messageId: String, emoji: Emoji) {
        presenter.react(messageId, emoji.shortname)
    }

    override fun showEditingAction(roomId: String, messageId: String, text: String) {
        val editor = activity?.getSharedPreferences("pinFunction", MODE_PRIVATE)!!.edit()
        editor.putString("action","edit")
        editor.putString("roomId", roomId)
        editor.putString("messageId", messageId)
        editor.putString("text", text)
        editor.apply()
        (activity as PinnedMessagesActivity).finishActivity()
    }

    override fun showReplyingAction(username: String, replyMarkdown: String, quotedMessage: String) {
        val editor = activity?.getSharedPreferences("pinFunction", MODE_PRIVATE)!!.edit()
        editor.putString("action","reply")
        editor.putString("username", username)
        editor.putString("replyMarkdown", replyMarkdown)
        editor.putString("quotedMessage", quotedMessage)
        editor.apply()
        (activity as PinnedMessagesActivity).finishActivity()
    }



    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showPinnedMessages(pinnedMessages: List<BaseViewModel<*>>) {
        activity?.apply {
            if (recycler_view_pinned.adapter == null) {
                // TODO - add a better constructor for this case...
                adapter = ChatRoomAdapter(chatRoomType, chatRoomName, null, presenter, false)
                recycler_view_pinned.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                recycler_view_pinned.layoutManager = linearLayoutManager
                recycler_view_pinned.itemAnimator = DefaultItemAnimator()
                if (pinnedMessages.size > 10) {
                    recycler_view_pinned.addOnScrollListener(object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
                        override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView?) {
                            presenter.loadPinnedMessages(chatRoomId)
                        }
                    })
                }
            }

            adapter.appendData(pinnedMessages)
        }
    }
}