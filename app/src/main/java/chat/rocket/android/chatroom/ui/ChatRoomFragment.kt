package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.extensions.*
import chat.rocket.android.widget.emoji.ComposerEditText
import chat.rocket.android.widget.emoji.Emoji
import chat.rocket.android.widget.emoji.EmojiKeyboardPopup
import chat.rocket.android.widget.emoji.EmojiParser
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_chat_room.*
import kotlinx.android.synthetic.main.message_attachment_options.*
import kotlinx.android.synthetic.main.message_composer.*
import kotlinx.android.synthetic.main.message_list.*
import timber.log.Timber
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
private const val REQUEST_CODE_FOR_PERFORM_SAF = 42

class ChatRoomFragment : Fragment(), ChatRoomView, EmojiKeyboardPopup.Listener {
    @Inject lateinit var presenter: ChatRoomPresenter
    @Inject lateinit var parser: MessageParser
    private lateinit var adapter: ChatRoomAdapter

    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private lateinit var emojiKeyboardPopup: EmojiKeyboardPopup
    private var isChatRoomReadOnly: Boolean = false

    private lateinit var actionSnackbar: ActionSnackbar
    private var citation: String? = null
    private var editingMessageId: String? = null

    private val compositeDisposable = CompositeDisposable()
    private var playComposeMessageButtonsAnimation = true

    // For reveal and unreveal anim.
    private val hypotenuse by lazy { Math.hypot(root_layout.width.toDouble(), root_layout.height.toDouble()).toFloat() }
    private val max by lazy { Math.max(layout_message_attachment_options.width.toDouble(), layout_message_attachment_options.height.toDouble()).toFloat() }
    private val centerX by lazy { recycler_view.right }
    private val centerY by lazy { recycler_view.bottom }
    private val handler = Handler()

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

        setupRecyclerView()
        setupFab()
        setupMessageComposer()
        setupActionSnackbar()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        text_message.addTextChangedListener(EmojiKeyboardPopup.EmojiTextWatcher(text_message))
    }

    override fun onDestroyView() {
        presenter.unsubscribeMessages()
        handler.removeCallbacksAndMessages(null)
        unsubscribeTextMessage()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_CODE_FOR_PERFORM_SAF && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                uploadFile(resultData.data)
            }
        }
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

    override fun showMessages(dataSet: List<BaseViewModel<*>>) {
        activity?.apply {
            if (recycler_view.adapter == null) {
                adapter = ChatRoomAdapter(chatRoomType, chatRoomName, presenter)
                recycler_view.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                linearLayoutManager.stackFromEnd = true
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

            val oldMessagesCount = adapter.itemCount
            adapter.appendData(dataSet)
            if (oldMessagesCount == 0 && dataSet.isNotEmpty()) {
                recycler_view.scrollToPosition(0)
            }
        }
    }

    override fun sendMessage(text: String) {
        if (!text.isBlank()) {
            presenter.sendMessage(chatRoomId, text, editingMessageId)
        }
    }

    override fun uploadFile(uri: Uri) {
        // TODO Just leaving a blank message that comes with the file for now. In the future lets add the possibility to add a message with the file to be uploaded.
        presenter.uploadFile(chatRoomId, uri, "")
    }

    override fun showInvalidFileMessage() = showMessage(getString(R.string.msg_invalid_file))

    override fun showNewMessage(message: List<BaseViewModel<*>>) {
        adapter.prependData(message)
        recycler_view.scrollToPosition(0)
    }

    override fun disableSendMessageButton() {
        button_send.isEnabled = false
    }

    override fun enableSendMessageButton() {
        button_send.isEnabled = true
        text_message.isEnabled = true
        text_message.erase()
    }

    override fun clearMessageComposition() {
        citation = null
        editingMessageId = null
        text_message.textContent = ""
        actionSnackbar.dismiss()
    }

    override fun dispatchUpdateMessage(index: Int, message: List<BaseViewModel<*>>) {
        adapter.updateItem(message.last())
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
            clipboard.primaryClip = ClipData.newPlainText("", message)
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

    override fun onEmojiAdded(emoji: Emoji) {
        val cursorPosition = text_message.selectionStart
        if (cursorPosition > -1) {
            text_message.text.insert(cursorPosition, EmojiParser.parse(emoji.shortname))
            text_message.setSelection(cursorPosition + emoji.unicode.length)
        }
    }

    override fun onNonEmojiKeyPressed(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> with(text_message) {
                if (selectionStart > 0) text.delete(selectionStart - 1, selectionStart)
            }
            else -> throw IllegalArgumentException("pressed key not expected")
        }
    }

    private fun setReactionButtonIcon(@DrawableRes drawableId: Int) {
        button_add_reaction.setImageResource(drawableId)
        button_add_reaction.setTag(drawableId)
    }

    override fun showFileSelection(filter: Array<String>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, filter)
        startActivityForResult(intent, REQUEST_CODE_FOR_PERFORM_SAF)
    }

    override fun showInvalidFileSize(fileSize: Int, maxFileSize: Int) {
        showMessage(getString(R.string.max_file_size_exceeded, fileSize, maxFileSize))
    }

    private fun setupRecyclerView() {
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("Scrolling vertically: $dy")
                if (!recyclerView.canScrollVertically(1)) {
                    button_fab.hide()
                } else {
                    if (dy < 0 && !button_fab.isVisible()) {
                        button_fab.show()
                    }
                }
            }
        })
    }

    private fun setupFab() {
        button_fab.setOnClickListener {
            recycler_view.scrollToPosition(0)
            button_fab.hide()
        }
    }

    private fun setupMessageComposer() {
        if (isChatRoomReadOnly) {
            text_room_is_read_only.setVisible(true)
            input_container.setVisible(false)
        } else {
            subscribeTextMessage()
            emojiKeyboardPopup = EmojiKeyboardPopup(activity!!, activity!!.findViewById(R.id.fragment_container))
            emojiKeyboardPopup.listener = this
            text_message.listener = object : ComposerEditText.ComposerEditTextListener {
                override fun onKeyboardOpened() {
                }

                override fun onKeyboardClosed() {
                    activity?.let {
                        if (!emojiKeyboardPopup.isKeyboardOpen) {
                            it.onBackPressed()
                        }
                        KeyboardHelper.hideSoftKeyboard(it)
                        emojiKeyboardPopup.dismiss()
                    }
                    setReactionButtonIcon(R.drawable.ic_reaction_24dp)
                }
            }

            button_send.setOnClickListener {
                var textMessage = citation ?: ""
                textMessage += text_message.textContent
                sendMessage(textMessage)

                clearMessageComposition()
            }

            button_show_attachment_options.setOnClickListener {
                if (layout_message_attachment_options.isShown) {
                    hideAttachmentOptions()
                } else {
                    showAttachmentOptions()
                }
            }

            view_dim.setOnClickListener {
                hideAttachmentOptions()
            }

            button_files.setOnClickListener {
                handler.postDelayed({
                    presenter.selectFile()
                }, 200)

                handler.postDelayed({
                    hideAttachmentOptions()
                }, 400)
            }

            button_add_reaction.setOnClickListener { view ->
                openEmojiKeyboardPopup()
            }
        }
    }

    private fun openEmojiKeyboardPopup() {
        if (!emojiKeyboardPopup.isShowing()) {
            // If keyboard is visible, simply show the  popup
            if (emojiKeyboardPopup.isKeyboardOpen) {
                emojiKeyboardPopup.showAtBottom()
            } else {
                // Open the text keyboard first and immediately after that show the emoji popup
                text_message.setFocusableInTouchMode(true)
                text_message.requestFocus()
                emojiKeyboardPopup.showAtBottomPending()
                KeyboardHelper.showSoftKeyboard(text_message)

            }
            setReactionButtonIcon(R.drawable.ic_keyboard_black_24dp)
        } else {
            // If popup is showing, simply dismiss it to show the undelying text keyboard
            emojiKeyboardPopup.dismiss()
            setReactionButtonIcon(R.drawable.ic_reaction_24dp)
        }
    }

    private fun setupActionSnackbar() {
        actionSnackbar = ActionSnackbar.make(message_list_container, parser = parser)
        actionSnackbar.cancelView.setOnClickListener({
            clearMessageComposition()
        })
    }

    private fun subscribeTextMessage() {
        val disposable = text_message.asObservable(0)
                .subscribe({ t -> setupComposeMessageButtons(t) })

        compositeDisposable.add(disposable)
    }

    private fun unsubscribeTextMessage() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    private fun setupComposeMessageButtons(charSequence: CharSequence) {
        if (charSequence.isNotEmpty() && playComposeMessageButtonsAnimation) {
            button_show_attachment_options.fadeOut(1F, 0F, 120)
            button_send.fadeIn(0F, 1F, 120)
            playComposeMessageButtonsAnimation = false
        }

        if (charSequence.isEmpty()) {
            button_send.fadeOut(1F, 0F, 120)
            button_show_attachment_options.fadeIn(0F, 1F, 120)
            playComposeMessageButtonsAnimation = true
        }
    }

    private fun showAttachmentOptions() {
        view_dim.setVisible(true)

        // Play anim.
        button_show_attachment_options.rotateBy(45F)
        layout_message_attachment_options.circularRevealOrUnreveal(centerX, centerY, 0F, hypotenuse)
    }

    private fun hideAttachmentOptions() {
        // Play anim.
        button_show_attachment_options.rotateBy(-45F)
        layout_message_attachment_options.circularRevealOrUnreveal(centerX, centerY, max, 0F)

        view_dim.setVisible(false)
    }
}