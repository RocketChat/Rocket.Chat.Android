package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.adapter.CommandSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.EmojiSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.PEOPLE
import chat.rocket.android.chatroom.adapter.PeopleSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.RoomSuggestionsAdapter
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.bottomsheet.MessageActionsBottomSheet
import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.EmojiSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.draw.main.ui.DRAWING_BYTE_ARRAY_EXTRA_DATA
import chat.rocket.android.draw.main.ui.DrawingActivity
import chat.rocket.android.emoji.ComposerEditText
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiKeyboardListener
import chat.rocket.android.emoji.EmojiKeyboardPopup
import chat.rocket.android.emoji.EmojiParser
import chat.rocket.android.emoji.EmojiPickerPopup
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.android.emoji.internal.isCustom
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.circularRevealOrUnreveal
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.hideKeyboard
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.rotateBy
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.realtime.socket.model.State
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat_room.*
import kotlinx.android.synthetic.main.message_attachment_options.*
import kotlinx.android.synthetic.main.message_composer.*
import kotlinx.android.synthetic.main.message_list.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

fun newInstance(
    chatRoomId: String,
    chatRoomName: String,
    chatRoomType: String,
    isReadOnly: Boolean,
    chatRoomLastSeen: Long,
    isSubscribed: Boolean = true,
    isCreator: Boolean = false,
    isFavorite: Boolean = false,
    chatRoomMessage: String? = null
): Fragment {
    return ChatRoomFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
            putBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY, isReadOnly)
            putLong(BUNDLE_CHAT_ROOM_LAST_SEEN, chatRoomLastSeen)
            putBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED, isSubscribed)
            putBoolean(BUNDLE_CHAT_ROOM_IS_CREATOR, isCreator)
            putBoolean(BUNDLE_CHAT_ROOM_IS_FAVORITE, isFavorite)
            putString(BUNDLE_CHAT_ROOM_MESSAGE, chatRoomMessage)
        }
    }
}

internal const val TAG_CHAT_ROOM_FRAGMENT = "ChatRoomFragment"

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"
private const val BUNDLE_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"
private const val REQUEST_CODE_FOR_PERFORM_SAF = 42
private const val REQUEST_CODE_FOR_DRAW = 101
private const val BUNDLE_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val BUNDLE_CHAT_ROOM_IS_SUBSCRIBED = "chat_room_is_subscribed"
private const val BUNDLE_CHAT_ROOM_IS_CREATOR = "chat_room_is_creator"
private const val BUNDLE_CHAT_ROOM_IS_FAVORITE = "chat_room_is_favorite"
private const val BUNDLE_CHAT_ROOM_MESSAGE = "chat_room_message"

internal const val MENU_ACTION_FAVORITE_UNFAVORITE_CHAT = 1
internal const val MENU_ACTION_MEMBER = 2
internal const val MENU_ACTION_MENTIONS = 3
internal const val MENU_ACTION_PINNED_MESSAGES = 4
internal const val MENU_ACTION_FAVORITE_MESSAGES = 5
internal const val MENU_ACTION_FILES = 6

class ChatRoomFragment : Fragment(), ChatRoomView, EmojiKeyboardListener, EmojiReactionListener,
    ChatRoomAdapter.OnActionSelected, Drawable.Callback {

    @Inject
    lateinit var presenter: ChatRoomPresenter
    @Inject
    lateinit var parser: MessageParser
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var adapter: ChatRoomAdapter
    internal lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    internal lateinit var chatRoomType: String
    private var newMessageCount: Int = 0
    private var chatRoomMessage: String? = null
    private var isSubscribed: Boolean = true
    private var isReadOnly: Boolean = false
    private var isCreator: Boolean = false
    internal var isFavorite: Boolean = false
    private var isBroadcastChannel: Boolean = false
    private lateinit var emojiKeyboardPopup: EmojiKeyboardPopup
    private var chatRoomLastSeen: Long = -1
    private lateinit var actionSnackbar: ActionSnackbar
    internal var citation: String? = null
    private var editingMessageId: String? = null
    internal var disableMenu: Boolean = false

    private val compositeDisposable = CompositeDisposable()
    private var playComposeMessageButtonsAnimation = true

    internal var isSearchTermQueried = false

    // For reveal and unreveal anim.
    private val hypotenuse by lazy {
        Math.hypot(
            root_layout.width.toDouble(),
            root_layout.height.toDouble()
        ).toFloat()
    }
    private val max by lazy {
        Math.max(
            layout_message_attachment_options.width.toDouble(),
            layout_message_attachment_options.height.toDouble()
        ).toFloat()
    }
    private val centerX by lazy { recycler_view.right }
    private val centerY by lazy { recycler_view.bottom }
    private val handler = Handler()
    private var verticalScrollOffset = AtomicInteger(0)

    private val dialogView by lazy { View.inflate(context, R.layout.file_attachments_dialog, null) }
    internal val alertDialog by lazy { AlertDialog.Builder(activity).setView(dialogView).create() }
    internal val imagePreview by lazy { dialogView.findViewById<ImageView>(R.id.image_preview) }
    internal val sendButton by lazy { dialogView.findViewById<Button>(R.id.button_send) }
    internal val cancelButton by lazy { dialogView.findViewById<Button>(R.id.button_cancel) }
    internal val description by lazy { dialogView.findViewById<EditText>(R.id.text_file_description) }
    internal val audioVideoAttachment by lazy { dialogView.findViewById<FrameLayout>(R.id.audio_video_attachment) }
    internal val textFile by lazy { dialogView.findViewById<TextView>(R.id.text_file_name) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomName = bundle.getString(BUNDLE_CHAT_ROOM_NAME)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
            isReadOnly = bundle.getBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY)
            isSubscribed = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED)
            chatRoomLastSeen = bundle.getLong(BUNDLE_CHAT_ROOM_LAST_SEEN)
            isCreator = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_CREATOR)
            isFavorite = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_FAVORITE)
            chatRoomMessage = bundle.getString(BUNDLE_CHAT_ROOM_MESSAGE)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }

        adapter = ChatRoomAdapter(chatRoomId, chatRoomType, chatRoomName, this, reactionListener = this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return container?.inflate(R.layout.fragment_chat_room)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(chatRoomName)

        presenter.setupChatRoom(chatRoomId, chatRoomName, chatRoomType, chatRoomMessage)
        presenter.loadChatRooms()
        setupRecyclerView()
        setupFab()
        setupSuggestionsView()
        setupActionSnackbar()
        (activity as ChatRoomActivity).let {
            it.showToolbarTitle(chatRoomName)
            it.showToolbarChatRoomIcon(chatRoomType)
        }

        analyticsManager.logScreenView(ScreenViewEvent.ChatRoom)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        text_message.addTextChangedListener(EmojiKeyboardPopup.EmojiTextWatcher(text_message))
    }

    override fun onDestroyView() {
        recycler_view.removeOnScrollListener(endlessRecyclerViewScrollListener)
        recycler_view.removeOnScrollListener(onScrollListener)
        recycler_view.removeOnLayoutChangeListener(layoutChangeListener)

        presenter.disconnect()
        presenter.saveUnfinishedMessage(chatRoomId, text_message.text.toString())
        handler.removeCallbacksAndMessages(null)
        unsubscribeComposeTextMessage()

        // Hides the keyboard (if it's opened) before going to any view.
        activity?.apply {
            hideKeyboard()
        }
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        setReactionButtonIcon(R.drawable.ic_reaction_24dp)
        dismissEmojiKeyboard()
        activity?.invalidateOptionsMenu()
    }

    private fun dismissEmojiKeyboard() {
        // Check if the keyboard was ever initialized.
        // It may be the case when you are looking a not joined room
        if (::emojiKeyboardPopup.isInitialized) {
            emojiKeyboardPopup.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultData != null && resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_FOR_PERFORM_SAF -> showFileAttachmentDialog(resultData.data)
                REQUEST_CODE_FOR_DRAW -> showDrawAttachmentDialog(
                    resultData.getByteArrayExtra(DRAWING_BYTE_ARRAY_EXTRA_DATA)
                )
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        setupMenu(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setOnMenuItemClickListener(item)
        return true
    }

    override fun showFavoriteIcon(isFavorite: Boolean) {
        this.isFavorite = isFavorite
        activity?.invalidateOptionsMenu()
    }

    override fun showMessages(dataSet: List<BaseUiModel<*>>, clearDataSet: Boolean) {
        ui {
            if (clearDataSet) {
                adapter.clearData()
            }

            if (dataSet.isNotEmpty()) {
                var prevMsgModel = dataSet[0]

                // track the message sent immediately after the current message
                var prevMessageUiModel: MessageUiModel? = null

                // Checking for all messages to assign true to the required showDayMaker
                // Loop over received messages to determine first unread
                var firstUnread = false
                for (i in dataSet.indices) {
                    val msgModel = dataSet[i]

                    if (i > 0) {
                        prevMsgModel = dataSet[i - 1]
                    }

                    val currentDayMarkerText = msgModel.currentDayMarkerText
                    val previousDayMarkerText = prevMsgModel.currentDayMarkerText
                    if (previousDayMarkerText != currentDayMarkerText) {
                        prevMsgModel.showDayMarker = true
                    }

                    if (!firstUnread && msgModel is MessageUiModel) {
                        val msg = msgModel.rawData
                        if (msg.timestamp < chatRoomLastSeen) {
                            // This message was sent before the last seen of the room. Hence, it was seen.
                            // if there is a message after (below) this, mark it firstUnread.
                            if (prevMessageUiModel != null) {
                                prevMessageUiModel.isFirstUnread = true
                            }
                            // Found first unread message.
                            firstUnread = true
                        }
                        prevMessageUiModel = msgModel
                    }
                }
            }

            if (recycler_view.adapter == null) {
                recycler_view.adapter = adapter
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(endlessRecyclerViewScrollListener)
                }
                recycler_view.addOnLayoutChangeListener(layoutChangeListener)
                recycler_view.addOnScrollListener(onScrollListener)

                // Load just once, on the first page...
                presenter.loadActiveMembers(chatRoomId, chatRoomType, filterSelfOut = true)
            }

            val oldMessagesCount = adapter.itemCount
            adapter.appendData(dataSet)
            if (oldMessagesCount == 0 && dataSet.isNotEmpty()) {
                recycler_view.scrollToPosition(0)
                verticalScrollOffset.set(0)
            }
            presenter.loadActiveMembers(chatRoomId, chatRoomType, filterSelfOut = true)
            empty_chat_view.isVisible = adapter.itemCount == 0
        }
    }

    override fun showSearchedMessages(dataSet: List<BaseUiModel<*>>) {
        recycler_view.removeOnScrollListener(endlessRecyclerViewScrollListener)
        adapter.clearData()
        adapter.prependData(dataSet)
        empty_chat_view.isVisible = adapter.itemCount == 0
    }

    override fun onRoomUpdated(
        userCanPost: Boolean,
        channelIsBroadcast: Boolean,
        userCanMod: Boolean
    ) {
        // TODO: We should rely solely on the user being able to post, but we cannot guarantee
        // that the "(channels|groups).roles" endpoint is supported by the server in use.
        ui {
            setupMessageComposer(userCanPost)
            isBroadcastChannel = channelIsBroadcast
            if (isBroadcastChannel && !userCanMod) {
                disableMenu = true
                activity?.invalidateOptionsMenu()
            }
        }
    }

    private val layoutChangeListener =
        View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            val y = oldBottom - bottom
            if (Math.abs(y) > 0 && isAdded) {
                // if y is positive the keyboard is up else it's down
                recycler_view.post {
                    if (y > 0 || Math.abs(verticalScrollOffset.get()) >= Math.abs(y)) {
                        ui { recycler_view.scrollBy(0, y) }
                    } else {
                        ui { recycler_view.scrollBy(0, verticalScrollOffset.get()) }
                    }
                }
            }
        }

    private lateinit var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        var state = AtomicInteger(RecyclerView.SCROLL_STATE_IDLE)

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    if (!state.compareAndSet(RecyclerView.SCROLL_STATE_SETTLING, newState)) {
                        state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState)
                    }
                }
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState)
                }
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState)
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (state.get() != RecyclerView.SCROLL_STATE_IDLE) {
                verticalScrollOffset.getAndAdd(dy)
            }
        }
    }

    private val fabScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(1)) {
                text_count.isVisible = false
                button_fab.hide()
                newMessageCount = 0
            } else {
                if (dy < 0 && !button_fab.isVisible) {
                    button_fab.show()
                    if (newMessageCount != 0) text_count.isVisible = true
                }
            }
        }
    }

    override fun sendMessage(text: String) {
        ui {
            if (!text.isBlank()) {
                if (!text.startsWith("/")) {
                    presenter.sendMessage(chatRoomId, text, editingMessageId)
                } else {
                    presenter.runCommand(text, chatRoomId)
                }
            }
        }
    }

    override fun showTypingStatus(usernameList: List<String>) {
        ui {
            when (usernameList.size) {
                1 -> text_typing_status.text =
                    SpannableStringBuilder()
                        .bold { append(usernameList[0]) }
                        .append(getString(R.string.msg_is_typing))
                2 -> text_typing_status.text =
                    SpannableStringBuilder()
                        .bold { append(usernameList[0]) }
                        .append(getString(R.string.msg_and))
                        .bold { append(usernameList[1]) }
                        .append(getString(R.string.msg_are_typing))

                else -> text_typing_status.text = getString(R.string.msg_several_users_are_typing)
            }
            text_typing_status.isVisible = true
        }
    }

    override fun hideTypingStatusView() {
        ui {
            text_typing_status.isVisible = false
        }
    }

    override fun showInvalidFileMessage() {
        showMessage(getString(R.string.msg_invalid_file))
    }

    override fun showNewMessage(message: List<BaseUiModel<*>>, isMessageReceived: Boolean) {
        ui {
            adapter.prependData(message)
            if (isMessageReceived && button_fab.isVisible) {
                newMessageCount++

                if (newMessageCount <= 99)
                    text_count.text = newMessageCount.toString()
                else
                    text_count.text = "99+"

                text_count.isVisible = true
            } else if (!button_fab.isVisible)
                recycler_view.scrollToPosition(0)
            verticalScrollOffset.set(0)
            empty_chat_view.isVisible = adapter.itemCount == 0
        }
    }

    override fun disableSendMessageButton() {
        ui {
            button_send.isEnabled = false
        }
    }

    override fun enableSendMessageButton() {
        ui {
            button_send.isEnabled = true
            text_message.isEnabled = true
            clearMessageComposition(true)
        }
    }


    override fun clearMessageComposition(deleteMessage: Boolean) {
        ui {
            citation = null
            editingMessageId = null
            if (deleteMessage) {
                text_message.textContent = ""
            }
            actionSnackbar.dismiss()
        }
    }

    override fun dispatchUpdateMessage(index: Int, message: List<BaseUiModel<*>>) {
        ui {
            if (adapter.updateItem(message.last())) {
                if (message.size > 1) {
                    adapter.prependData(listOf(message.first()))
                }
            } else {
                showNewMessage(message, true)
            }
        }
    }

    override fun dispatchDeleteMessage(msgId: String) {
        ui {
            adapter.removeItem(msgId)
        }
    }

    override fun showReplyingAction(username: String, replyMarkdown: String, quotedMessage: String) {
        ui {
            citation = replyMarkdown
            actionSnackbar.title = username
            actionSnackbar.text = quotedMessage
            actionSnackbar.show()
            KeyboardHelper.showSoftKeyboard(text_message)
        }
    }

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun populatePeopleSuggestions(members: List<PeopleSuggestionUiModel>) {
        ui {
            suggestions_view.addItems("@", members)
        }
    }

    override fun populateRoomSuggestions(chatRooms: List<ChatRoomSuggestionUiModel>) {
        ui {
            suggestions_view.addItems("#", chatRooms)
        }
    }

    override fun populateCommandSuggestions(commands: List<CommandSuggestionUiModel>) {
        ui {
            suggestions_view.addItems("/", commands)
        }
    }

    override fun populateEmojiSuggestions(emojis: List<EmojiSuggestionUiModel>) {
        ui {
            suggestions_view.addItems(":", emojis)
        }
    }

    override fun copyToClipboard(message: String) {
        ui {
            val clipboard = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("", message)
            showToast(R.string.msg_message_copied)
        }
    }

    override fun showEditingAction(roomId: String, messageId: String, text: String) {
        ui {
            actionSnackbar.title = getString(R.string.action_title_editing)
            actionSnackbar.text = text
            actionSnackbar.show()
            text_message.textContent = text
            editingMessageId = messageId
            KeyboardHelper.showSoftKeyboard(text_message)
        }
    }

    override fun onEmojiAdded(emoji: Emoji) {
        val cursorPosition = text_message.selectionStart
        if (cursorPosition > -1) {
            context?.let {
                val offset = if (!emoji.isCustom()) emoji.unicode.length else emoji.shortname.length
                val parsed = if (emoji.isCustom()) emoji.shortname else EmojiParser.parse(it, emoji.shortname)
                text_message.text?.insert(cursorPosition, parsed)
                text_message.setSelection(cursorPosition + offset)
            }
        }
    }

    override fun onNonEmojiKeyPressed(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> with(text_message) {
                if (selectionStart > 0) text?.delete(selectionStart - 1, selectionStart)
            }
            else -> throw IllegalArgumentException("pressed key not expected")
        }
    }

    override fun onReactionTouched(messageId: String, emojiShortname: String) {
        presenter.react(messageId, emojiShortname)
    }

    override fun onReactionAdded(messageId: String, emoji: Emoji) {
        presenter.react(messageId, emoji.shortname)
    }

    override fun showReactionsPopup(messageId: String) {
        ui {
            val emojiPickerPopup = EmojiPickerPopup(it)
            emojiPickerPopup.listener = object : EmojiKeyboardListener {
                override fun onEmojiAdded(emoji: Emoji) {
                    onReactionAdded(messageId, emoji)
                }
            }
            emojiPickerPopup.show()
        }
    }

    private fun setReactionButtonIcon(@DrawableRes drawableId: Int) {
        button_add_reaction.setImageResource(drawableId)
        button_add_reaction.tag = drawableId
    }

    override fun showFileSelection(filter: Array<String>?) {
        ui {
            val intent = Intent(Intent.ACTION_GET_CONTENT)

            // Must set a type otherwise the intent won't resolve
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            // Filter selectable files to those that match the whitelist for this particular server
            if (filter != null) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, filter)
            }
            startActivityForResult(intent, REQUEST_CODE_FOR_PERFORM_SAF)
        }
    }

    override fun showInvalidFileSize(fileSize: Int, maxFileSize: Int) {
        showMessage(getString(R.string.max_file_size_exceeded, fileSize, maxFileSize))
    }

    override fun showConnectionState(state: State) {
        ui {
            text_connection_status.fadeIn()
            handler.removeCallbacks(dismissStatus)
            when (state) {
                is State.Connected -> {
                    text_connection_status.text = getString(R.string.status_connected)
                    handler.postDelayed(dismissStatus, 2000)
                }
                is State.Disconnected ->
                    text_connection_status.text = getString(R.string.status_disconnected)
                is State.Connecting ->
                    text_connection_status.text = getString(R.string.status_connecting)
                is State.Authenticating ->
                    text_connection_status.text = getString(R.string.status_authenticating)
                is State.Disconnecting ->
                    text_connection_status.text = getString(R.string.status_disconnecting)
                is State.Waiting ->
                    text_connection_status.text = getString(R.string.status_waiting, state.seconds)
            }
        }
    }

    override fun onJoined(userCanPost: Boolean) {
        ui {
            input_container.isVisible = true
            button_join_chat.isVisible = false
            isSubscribed = true
            setupMessageComposer(userCanPost)
        }
    }

    private val dismissStatus = {
        text_connection_status.fadeOut()
    }

    private fun setupRecyclerView() {
        // Initialize the endlessRecyclerViewScrollListener so we don't NPE at onDestroyView
        val linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        recycler_view.layoutManager = linearLayoutManager
        recycler_view.itemAnimator = DefaultItemAnimator()
        endlessRecyclerViewScrollListener = object :
            EndlessRecyclerViewScrollListener(recycler_view.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, recyclerView: RecyclerView) {
                presenter.loadMessages(chatRoomId, chatRoomType, page * 30L)
            }
        }
        recycler_view.addOnScrollListener(fabScrollListener)
    }

    private fun setupFab() {
        button_fab.setOnClickListener {
            recycler_view.scrollToPosition(0)
            verticalScrollOffset.set(0)
            text_count.isVisible = false
            button_fab.hide()
            newMessageCount = 0
        }
    }

    private fun setupMessageComposer(canPost: Boolean) {
        if (isReadOnly && !canPost) {
            text_room_is_read_only.isVisible = true
            input_container.isVisible = false
        } else if (!isSubscribed && roomTypeOf(chatRoomType) !is RoomType.DirectMessage) {
            input_container.isVisible = false
            button_join_chat.isVisible = true
            button_join_chat.setOnClickListener { presenter.joinChat(chatRoomId) }
        } else {
            button_send.isVisible = false
            button_show_attachment_options.alpha = 1f
            button_show_attachment_options.isVisible = true

            activity?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentAttached(
                        fm: FragmentManager,
                        f: Fragment,
                        context: Context
                    ) {
                        if (f is MessageActionsBottomSheet) {
                            setReactionButtonIcon(R.drawable.ic_reaction_24dp)
                            emojiKeyboardPopup.dismiss()
                        }
                    }
                },
                true
            )

            subscribeComposeTextMessage()
            getUnfinishedMessage()
            emojiKeyboardPopup = EmojiKeyboardPopup(activity!!, activity!!.findViewById(R.id.fragment_container))
            emojiKeyboardPopup.listener = this
            text_message.listener = object : ComposerEditText.ComposerEditTextListener {
                override fun onKeyboardOpened() {}

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
                clearMessageComposition(true)
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

            button_add_reaction.setOnClickListener { _ ->
                openEmojiKeyboardPopup()
            }

            button_drawing.setOnClickListener {
                activity?.let { fragmentActivity ->
                    if (!ImageHelper.canWriteToExternalStorage(fragmentActivity)) {
                        ImageHelper.checkWritingPermission(fragmentActivity)
                    } else {
                        val intent = Intent(fragmentActivity, DrawingActivity::class.java)
                        startActivityForResult(intent, REQUEST_CODE_FOR_DRAW)
                    }
                }

                handler.postDelayed({
                    hideAttachmentOptions()
                }, 400)
            }
        }
    }

    private fun getUnfinishedMessage() {
        val unfinishedMessage = presenter.getUnfinishedMessage(chatRoomId)
        if (unfinishedMessage.isNotBlank()) {
            text_message.setText(unfinishedMessage)
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                KeyboardHelper.showSoftKeyboard(text_message)
            } else {
                //TODO show keyboard in full screen mode when landscape orientation
            }
        }
    }

    private fun setupSuggestionsView() {
        suggestions_view.anchorTo(text_message)
            .setMaximumHeight(resources.getDimensionPixelSize(R.dimen.suggestions_box_max_height))
            .addTokenAdapter(PeopleSuggestionsAdapter(context!!))
            .addTokenAdapter(CommandSuggestionsAdapter())
            .addTokenAdapter(RoomSuggestionsAdapter())
            .addTokenAdapter(EmojiSuggestionsAdapter())
            .addSuggestionProviderAction("@") { query ->
                if (query.isNotEmpty()) {
                    presenter.spotlight(query, PEOPLE, true)
                }
            }
            .addSuggestionProviderAction("#") { query ->
                if (query.isNotEmpty()) {
                    presenter.loadChatRooms()
                }
            }
            .addSuggestionProviderAction("/") {
                presenter.loadCommands()
            }
            .addSuggestionProviderAction(":") {
                presenter.loadEmojis()
            }

        presenter.loadEmojis()
        presenter.loadCommands()
    }

    private fun openEmojiKeyboardPopup() {
        if (!emojiKeyboardPopup.isShowing) {
            // If keyboard is visible, simply show the  popup
            if (emojiKeyboardPopup.isKeyboardOpen) {
                emojiKeyboardPopup.showAtBottom()
            } else {
                // Open the text keyboard first and immediately after that show the emoji popup
                text_message.isFocusableInTouchMode = true
                text_message.requestFocus()
                emojiKeyboardPopup.showAtBottomPending()
                KeyboardHelper.showSoftKeyboard(text_message)
            }
            setReactionButtonIcon(R.drawable.ic_keyboard_black_24dp)
        } else {
            // If popup is showing, simply dismiss it to show the underlying text keyboard
            emojiKeyboardPopup.dismiss()
            setReactionButtonIcon(R.drawable.ic_reaction_24dp)
        }
    }

    private fun setupActionSnackbar() {
        actionSnackbar = ActionSnackbar.make(message_list_container, parser = parser)
        actionSnackbar.cancelView.setOnClickListener {
            clearMessageComposition(false)
            if (text_message.textContent.isEmpty()) {
                KeyboardHelper.showSoftKeyboard(text_message)
            }
        }
    }

    private fun subscribeComposeTextMessage() {
        val editTextObservable = text_message.asObservable()

        compositeDisposable.addAll(
            subscribeComposeButtons(editTextObservable),
            subscribeComposeTypingStatus(editTextObservable)
        )
    }

    private fun unsubscribeComposeTextMessage() {
        compositeDisposable.clear()
    }

    private fun subscribeComposeButtons(observable: Observable<CharSequence>): Disposable {
        return observable.subscribe { t -> setupComposeButtons(t) }
    }

    private fun subscribeComposeTypingStatus(observable: Observable<CharSequence>): Disposable {
        return observable.debounce(300, TimeUnit.MILLISECONDS)
            .skip(1)
            .subscribe { t -> sendTypingStatus(t) }
    }

    private fun setupComposeButtons(charSequence: CharSequence) {
        if (charSequence.isNotEmpty() && playComposeMessageButtonsAnimation) {
            button_show_attachment_options.isVisible = false
            button_send.isVisible = true
            playComposeMessageButtonsAnimation = false
        }

        if (charSequence.isEmpty()) {
            button_send.isVisible = false
            button_show_attachment_options.isVisible = true
            playComposeMessageButtonsAnimation = true
        }
    }

    private fun sendTypingStatus(charSequence: CharSequence) {
        if (charSequence.isNotBlank()) {
            presenter.sendTyping()
        } else {
            presenter.sendNotTyping()
        }
    }

    private fun showAttachmentOptions() {
        view_dim.isVisible = true

        // Play anim.
        button_show_attachment_options.rotateBy(45F)
        layout_message_attachment_options.circularRevealOrUnreveal(centerX, centerY, 0F, hypotenuse)
    }

    private fun hideAttachmentOptions() {
        // Play anim.
        button_show_attachment_options.rotateBy(-45F)
        layout_message_attachment_options.circularRevealOrUnreveal(centerX, centerY, max, 0F)

        view_dim.isVisible = false
    }

    private fun setupToolbar(toolbarTitle: String) {
        (activity as ChatRoomActivity).showToolbarTitle(toolbarTitle)
    }

    override fun unscheduleDrawable(who: Drawable?, what: Runnable?) {
        text_message?.removeCallbacks(what)
    }

    override fun invalidateDrawable(who: Drawable?) {
        text_message?.invalidate()
    }

    override fun scheduleDrawable(who: Drawable?, what: Runnable?, `when`: Long) {
        text_message?.postDelayed(what, `when`)
    }

    override fun showMessageInfo(id: String) {
        presenter.messageInfo(id)
    }

    override fun citeMessage(
        roomName: String,
        roomType: String,
        messageId: String,
        mentionAuthor: Boolean
    ) {
        presenter.citeMessage(roomName, roomType, messageId, mentionAuthor)
    }

    override fun copyMessage(id: String) {
        presenter.copyMessage(id)
    }

    override fun editMessage(roomId: String, messageId: String, text: String) {
        presenter.editMessage(roomId, messageId, text)
    }

    override fun toogleStar(id: String, star: Boolean) {
        if (star) {
            presenter.starMessage(id)
        } else {
            presenter.unstarMessage(id)
        }
    }

    override fun tooglePin(id: String, pin: Boolean) {
        if (pin) {
            presenter.pinMessage(id)
        } else {
            presenter.unpinMessage(id)
        }
    }

    override fun deleteMessage(roomId: String, id: String) {
        ui {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(it.getString(R.string.msg_delete_message))
                .setMessage(it.getString(R.string.msg_delete_description))
                .setPositiveButton(it.getString(R.string.msg_ok)) { _, _ ->
                    presenter.deleteMessage(
                        roomId,
                        id
                    )
                }
                .setNegativeButton(it.getString(R.string.msg_cancel)) { _, _ -> }
                .show()
        }
    }

    override fun showReactions(id: String) {
        presenter.showReactions(id)
    }

    override fun openDirectMessage(roomName: String, message: String) {
        presenter.openDirectMessage(roomName, message)
    }

    override fun sendMessage(chatRoomId: String, text: String) {
        presenter.sendMessage(chatRoomId, text, null)
    }
}
