package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.chatroom.adapter.AttachmentViewHolder
import chat.rocket.android.chatroom.adapter.ChatRoomAdapter
import chat.rocket.android.chatroom.adapter.CommandSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.EmojiSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.MessageViewHolder
import chat.rocket.android.chatroom.adapter.PEOPLE
import chat.rocket.android.chatroom.adapter.PeopleSuggestionsAdapter
import chat.rocket.android.chatroom.adapter.RoomSuggestionsAdapter
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.bottomsheet.MessageActionsBottomSheet
import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.EmojiSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
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
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.helper.AndroidPermissionsHelper
import chat.rocket.android.helper.AndroidPermissionsHelper.getCameraPermission
import chat.rocket.android.helper.AndroidPermissionsHelper.getWriteExternalStoragePermission
import chat.rocket.android.helper.AndroidPermissionsHelper.hasCameraPermission
import chat.rocket.android.helper.AndroidPermissionsHelper.hasWriteExternalStoragePermission
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extension.createImageFile
import chat.rocket.android.util.extension.orFalse
import chat.rocket.android.util.extensions.circularRevealOrUnreveal
import chat.rocket.android.util.extensions.clearLightStatusBar
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.hideKeyboard
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.android.util.extensions.rotateBy
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.realtime.socket.model.State
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.emoji_image_row_item.view.*
import kotlinx.android.synthetic.main.emoji_row_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_room.*
import kotlinx.android.synthetic.main.message_attachment_options.*
import kotlinx.android.synthetic.main.message_composer.*
import kotlinx.android.synthetic.main.message_list.*
import kotlinx.android.synthetic.main.reaction_praises_list_item.view.*
import timber.log.Timber
import java.io.File
import java.io.IOException
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
): Fragment = ChatRoomFragment().apply {
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

internal const val TAG_CHAT_ROOM_FRAGMENT = "ChatRoomFragment"

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"
private const val BUNDLE_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"
private const val REQUEST_CODE_FOR_PERFORM_SAF = 42
private const val REQUEST_CODE_FOR_DRAW = 101
private const val REQUEST_CODE_FOR_PERFORM_CAMERA = 102
private const val BUNDLE_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val BUNDLE_CHAT_ROOM_IS_SUBSCRIBED = "chat_room_is_subscribed"
private const val BUNDLE_CHAT_ROOM_IS_CREATOR = "chat_room_is_creator"
private const val BUNDLE_CHAT_ROOM_IS_FAVORITE = "chat_room_is_favorite"
private const val BUNDLE_CHAT_ROOM_MESSAGE = "chat_room_message"

class ChatRoomFragment : Fragment(), ChatRoomView, EmojiKeyboardListener, EmojiReactionListener,
    ChatRoomAdapter.OnActionSelected, Drawable.Callback {
    @Inject
    lateinit var presenter: ChatRoomPresenter
    @Inject
    lateinit var parser: MessageParser
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var navigator: ChatRoomNavigator
    private lateinit var chatRoomAdapter: ChatRoomAdapter
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
    private var disableMenu: Boolean = false

    private val compositeDisposable = CompositeDisposable()
    private var playComposeMessageButtonsAnimation = true

    internal var isSearchTermQueried = false
    private val dismissConnectionState by lazy { text_connection_status.fadeOut() }

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
    internal val alertDialog by lazy {
        activity?.let {
            AlertDialog.Builder(it).setView(dialogView).create()
        }
    }
    internal val imagePreview by lazy { dialogView.findViewById<ImageView>(R.id.image_preview) }
    internal val sendButton by lazy { dialogView.findViewById<android.widget.Button>(R.id.button_send) }
    internal val cancelButton by lazy { dialogView.findViewById<android.widget.Button>(R.id.button_cancel) }
    internal val description by lazy { dialogView.findViewById<EditText>(R.id.text_file_description) }
    internal val audioVideoAttachment by lazy { dialogView.findViewById<FrameLayout>(R.id.audio_video_attachment) }
    internal val textFile by lazy { dialogView.findViewById<TextView>(R.id.text_file_name) }
    private var takenPhotoUri: Uri? = null

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
                if (dy < 0 && isAdded && !button_fab.isVisible) {
                    button_fab.show()
                    if (newMessageCount != 0) text_count.isVisible = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            chatRoomId = getString(BUNDLE_CHAT_ROOM_ID, "")
            chatRoomName = getString(BUNDLE_CHAT_ROOM_NAME, "")
            chatRoomType = getString(BUNDLE_CHAT_ROOM_TYPE, "")
            isReadOnly = getBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY)
            isSubscribed = getBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED)
            chatRoomLastSeen = getLong(BUNDLE_CHAT_ROOM_LAST_SEEN)
            isCreator = getBoolean(BUNDLE_CHAT_ROOM_IS_CREATOR)
            isFavorite = getBoolean(BUNDLE_CHAT_ROOM_IS_FAVORITE)
            chatRoomMessage = getString(BUNDLE_CHAT_ROOM_MESSAGE)
        }
            ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }

        chatRoomAdapter = ChatRoomAdapter(
            roomId = chatRoomId,
            roomType = chatRoomType,
            roomName = chatRoomName,
            actionSelectListener = this,
            reactionListener = this,
            navigator = navigator,
            analyticsManager = analyticsManager
        )

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_chat_room)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(chatRoomName)

        presenter.setupChatRoom(chatRoomId, chatRoomName, chatRoomType, chatRoomMessage)
        presenter.loadChatRoomsSuggestions()
        setupRecyclerView()
        setupFab()
        setupSuggestionsView()
        setupActionSnackbar()
        with(activity as ChatRoomActivity) {
            setupToolbarTitle(chatRoomName)
            setupExpandMoreForToolbar {
                presenter.toChatDetails(
                    chatRoomId,
                    chatRoomType,
                    isSubscribed,
                    isFavorite,
                    disableMenu
                )
            }
        }
        getDraftMessage()
        subscribeComposeTextMessage()

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

        presenter.saveDraftMessage(text_message.text.toString())
        handler.removeCallbacksAndMessages(null)
        unsubscribeComposeTextMessage()
        presenter.disconnect()

        // Hides the keyboard (if it's opened) before going to any view.
        activity?.apply { hideKeyboard() }
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        dismissEmojiKeyboard()
        activity?.invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_FOR_PERFORM_CAMERA -> takenPhotoUri?.let {
                    showFileAttachmentDialog(it)
                }
                REQUEST_CODE_FOR_PERFORM_SAF -> resultData?.data?.let {
                    showFileAttachmentDialog(it)
                }
                REQUEST_CODE_FOR_DRAW -> resultData?.getByteArrayExtra(DRAWING_BYTE_ARRAY_EXTRA_DATA)?.let {
                    showDrawAttachmentDialog(it)
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        setupMenu(menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun openFullWebPage(roomId: String, url: String) {
        presenter.openFullWebPage(roomId, url)
    }

    override fun openConfigurableWebPage(roomId: String, url: String, heightRatio: String) {
        presenter.openConfigurableWebPage(roomId, url, heightRatio)
    }


    override fun showMessages(dataSet: List<BaseUiModel<*>>, clearDataSet: Boolean) {
        ui {
            if (clearDataSet) {
                chatRoomAdapter.clearData()
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

            val oldMessagesCount = chatRoomAdapter.itemCount
            chatRoomAdapter.appendData(dataSet)
            if (oldMessagesCount == 0 && dataSet.isNotEmpty()) {
                recycler_view.scrollToPosition(0)
                verticalScrollOffset.set(0)
            }
            empty_chat_view.isVisible = chatRoomAdapter.itemCount == 0
            dismissEmojiKeyboard()
        }
    }

    override fun showSearchedMessages(dataSet: List<BaseUiModel<*>>) {
        recycler_view.removeOnScrollListener(endlessRecyclerViewScrollListener)
        chatRoomAdapter.clearData()
        chatRoomAdapter.prependData(dataSet)
        empty_chat_view.isVisible = chatRoomAdapter.itemCount == 0
        dismissEmojiKeyboard()
    }

    override fun onRoomUpdated(roomUiModel: RoomUiModel) {
        // TODO: We should rely solely on the user being able to post, but we cannot guarantee
        // that the "(channels|groups).getPermissionRoles" endpoint is supported by the server in use.
        ui {
            setupToolbar(roomUiModel.name.toString())
            setupMessageComposer(roomUiModel)
            isBroadcastChannel = roomUiModel.broadcast
            isFavorite = roomUiModel.favorite.orFalse()
            disableMenu = (roomUiModel.broadcast && !roomUiModel.canModerate)
            activity?.invalidateOptionsMenu()
        }
    }

    override fun sendMessage(text: String) {
        ui {
            if (!text.isBlank()) {
                when {
                    text.startsWith("/") -> presenter.runCommand(text, chatRoomId)
                    text.startsWith("+") -> presenter.reactToLastMessage(text, chatRoomId)
                    else -> presenter.sendMessage(chatRoomId, text, editingMessageId)
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
        ui { text_typing_status.isVisible = false }
    }

    override fun showInvalidFileMessage() {
        showMessage(getString(R.string.msg_invalid_file))
    }

    override fun disableSendMessageButton() {
        ui { button_send.isEnabled = false }
    }

    override fun enableSendMessageButton() {
        ui {
            button_send.isEnabled = true
            text_message.isEnabled = true
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

    override fun showNewMessage(message: List<BaseUiModel<*>>, isMessageReceived: Boolean) {
        ui {
            chatRoomAdapter.prependData(message)
            if (isMessageReceived && button_fab.isVisible) {
                newMessageCount++
                if (newMessageCount <= 99) {
                    text_count.text = newMessageCount.toString()
                } else {
                    text_count.text = getString(R.string.msg_more_than_ninety_nine_unread_messages)
                }
                text_count.isVisible = true
            } else if (!button_fab.isVisible) {
                recycler_view.scrollToPosition(0)
            }
            verticalScrollOffset.set(0)
            empty_chat_view.isVisible = chatRoomAdapter.itemCount == 0
            dismissEmojiKeyboard()
        }
    }

    override fun dispatchUpdateMessage(index: Int, message: List<BaseUiModel<*>>) {
        ui {
            // TODO - investigate WHY we get a empty list here
            if (message.isEmpty()) return@ui

            when (chatRoomAdapter.updateItem(message.last())) {
                // FIXME: What's 0,1 and 2 means for here?
                0 -> {
                    if (message.size > 1) {
                        chatRoomAdapter.prependData(listOf(message.first()))
                    }
                }
                1 -> showNewMessage(message, true)
                2 -> {
                    // Position of new sent message is wrong because of device local time is behind server time
                    with(chatRoomAdapter) {
                        removeItem(message.last().messageId)
                        prependData(listOf(message.last()))
                        notifyDataSetChanged()
                    }
                }
            }
            dismissEmojiKeyboard()
        }
    }

    override fun dispatchDeleteMessage(msgId: String) {
        ui {
            chatRoomAdapter.removeItem(msgId)
        }
    }

    override fun showReplyingAction(
        username: String,
        replyMarkdown: String,
        quotedMessage: String
    ) {
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
        ui { showToast(message) }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showGenericErrorMessage() {
        ui { showMessage(getString(R.string.msg_generic_error)) }
    }

    override fun populatePeopleSuggestions(members: List<PeopleSuggestionUiModel>) {
        ui { suggestions_view.addItems("@", members) }
    }

    override fun populateRoomSuggestions(chatRooms: List<ChatRoomSuggestionUiModel>) {
        ui { suggestions_view.addItems("#", chatRooms) }
    }

    override fun populateCommandSuggestions(commands: List<CommandSuggestionUiModel>) {
        ui { suggestions_view.addItems("/", commands) }
    }

    override fun populateEmojiSuggestions(emojis: List<EmojiSuggestionUiModel>) {
        ui { suggestions_view.addItems(":", emojis) }
    }

    override fun copyToClipboard(message: String) {
        ui {
            (it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).apply {
                setPrimaryClip(ClipData.newPlainText("", message))
            }
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
                val parsed = if (emoji.isCustom()) emoji.shortname else EmojiParser.parse(
                    it,
                    emoji.shortname
                )
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

    override fun onReactionLongClicked(
        shortname: String,
        isCustom: Boolean,
        url: String?,
        usernames: List<String>
    ) {
        val layout =
            LayoutInflater.from(requireContext()).inflate(R.layout.reaction_praises_list_item, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(layout)
            .setCancelable(true)

        with(layout) {
            view_flipper.displayedChild = if (isCustom) 1 else 0
            if (isCustom && url != null) {
                val glideRequest = if (url.endsWith("gif", true)) {
                    Glide.with(requireContext()).asGif()
                } else {
                    Glide.with(requireContext()).asBitmap()
                }

                glideRequest.load(url).into(view_flipper.emoji_image_view)
            } else {
                view_flipper.emoji_view.text = EmojiParser.parse(requireContext(), shortname)
            }

            var listing = ""
            if (usernames.size == 1) {
                listing = usernames.first()
            } else {
                usernames.forEachIndexed { index, username ->
                    listing += if (index == usernames.size - 1) "|$username" else "$username, "
                }

                listing =
                    listing.replace(", |", " ${requireContext().getString(R.string.msg_and)} ")
            }

            text_view_usernames.text = requireContext().resources.getQuantityString(
                R.plurals.msg_reacted_with_, usernames.size, listing, shortname
            )

            dialog.show()
        }
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
        button_add_reaction_or_show_keyboard?.setImageResource(drawableId)
        button_add_reaction_or_show_keyboard?.tag = drawableId
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
            handler.removeCallbacks { dismissConnectionState }
            text_connection_status.text = when (state) {
                is State.Connected -> {
                    handler.postDelayed({ dismissConnectionState }, 2000)
                    getString(R.string.status_connected)
                }
                is State.Disconnected -> getString(R.string.status_disconnected)
                is State.Connecting -> getString(R.string.status_connecting)
                is State.Authenticating -> getString(R.string.status_authenticating)
                is State.Disconnecting -> getString(R.string.status_disconnecting)
                is State.Waiting -> getString(R.string.status_waiting, state.seconds)
                else -> "" // Show nothing
            }
        }
    }

    override fun onJoined(roomUiModel: RoomUiModel) {
        ui {
            input_container.isVisible = true
            button_join_chat.isVisible = false
            isSubscribed = true
        }
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

        with (recycler_view) {
            adapter = chatRoomAdapter
            addOnScrollListener(endlessRecyclerViewScrollListener)
            addOnLayoutChangeListener(layoutChangeListener)
            addOnScrollListener(onScrollListener)
            addOnScrollListener(fabScrollListener)
        }
        if (!isReadOnly) {
            val touchCallback: ItemTouchHelper.SimpleCallback =
                object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        var replyId: String? = null

                        when (viewHolder) {
                            is MessageViewHolder -> replyId = viewHolder.data?.messageId
                            is AttachmentViewHolder -> replyId = viewHolder.data?.messageId
                        }

                        replyId?.let {
                            citeMessage(chatRoomName, chatRoomType, it, true)
                        }

                        chatRoomAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    }

                    override fun getSwipeDirs(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ): Int {
                        // Currently enable swipes for text and attachment messages only

                        if (viewHolder is MessageViewHolder || viewHolder is AttachmentViewHolder) {
                            return super.getSwipeDirs(recyclerView, viewHolder)
                        }

                        return 0
                    }
                }

            ItemTouchHelper(touchCallback).attachToRecyclerView(recycler_view)
        }
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

    private fun setupMessageComposer(roomUiModel: RoomUiModel) {
        if (isReadOnly || !roomUiModel.writable) {
            text_room_is_read_only.isVisible = true
            input_container.isVisible = false
            text_room_is_read_only.setText(
                if (isReadOnly) {
                    R.string.msg_this_room_is_read_only
                } else {
                    // Not a read-only channel but user has been muted.
                    R.string.msg_muted_on_this_channel
                }
            )
        } else if (!isSubscribed && roomTypeOf(chatRoomType) !is RoomType.DirectMessage) {
            input_container.isVisible = false
            button_join_chat.isVisible = true
            button_join_chat.setOnClickListener { presenter.joinChat(chatRoomId) }
        } else {
            input_container.isVisible = true
            text_room_is_read_only.isVisible = false
            button_show_attachment_options.alpha = 1f

            activity?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentAttached(
                        fm: FragmentManager,
                        f: Fragment,
                        context: Context
                    ) {
                        if (f is MessageActionsBottomSheet) {
                            dismissEmojiKeyboard()
                        }
                    }
                },
                true
            )

            emojiKeyboardPopup =
                EmojiKeyboardPopup(activity!!, activity!!.findViewById(R.id.fragment_container))

            emojiKeyboardPopup.listener = this

            text_message.listener = object : ComposerEditText.ComposerEditTextListener {
                override fun onKeyboardOpened() {
                    KeyboardHelper.showSoftKeyboard(text_message)
                }

                override fun onKeyboardClosed() {
                    activity?.let {
                        if (!emojiKeyboardPopup.isKeyboardOpen) {
                            it.onBackPressed()
                        }
                        KeyboardHelper.hideSoftKeyboard(it)
                        dismissEmojiKeyboard()
                    }
                }
            }

            button_send.setOnClickListener {
                text_message.textContent.run {
                    if(this.isNotBlank()) {
                        sendMessage((citation ?: "") + this)
                    }
                }
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

            button_add_reaction_or_show_keyboard.setOnClickListener { toggleKeyboard() }

            button_take_a_photo.setOnClickListener {
                // Check for camera permission
                context?.let {
                    if (hasCameraPermission(it)) {
                        dispatchTakePictureIntent()
                    } else {
                        getCameraPermission(this)
                    }
                }
                handler.postDelayed({
                    hideAttachmentOptions()
                }, 400)
            }

            button_attach_a_file.setOnClickListener {
                handler.postDelayed({
                    presenter.selectFile()
                }, 200)

                handler.postDelayed({
                    hideAttachmentOptions()
                }, 400)
            }

            button_drawing.setOnClickListener {
                activity?.let { fragmentActivity ->
                    if (!hasWriteExternalStoragePermission(fragmentActivity)) {
                        getWriteExternalStoragePermission(this)
                    } else {
                        dispatchDrawingIntent()
                    }
                }

                handler.postDelayed({
                    hideAttachmentOptions()
                }, 400)
            }
        }
    }

    private fun dispatchDrawingIntent() {
        val intent = Intent(activity, DrawingActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FOR_DRAW)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                activity?.createImageFile()
            } catch (ex: IOException) {
                Timber.e(ex)
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                takenPhotoUri = FileProvider.getUriForFile(
                    requireContext(), "chat.rocket.android.fileprovider", it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takenPhotoUri)
                startActivityForResult(takePictureIntent, REQUEST_CODE_FOR_PERFORM_CAMERA)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AndroidPermissionsHelper.CAMERA_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    dispatchTakePictureIntent()
                } else {
                    // permission denied
                    Snackbar.make(
                        root_layout,
                        R.string.msg_camera_permission_denied,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            }
            AndroidPermissionsHelper.WRITE_EXTERNAL_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    dispatchDrawingIntent()
                } else {
                    // permission denied
                    Snackbar.make(
                        root_layout,
                        R.string.msg_storage_permission_denied,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun getDraftMessage() {
        val unfinishedMessage = presenter.getDraftUnfinishedMessage()
        if (unfinishedMessage.isNotNullNorEmpty()) {
            text_message.setText(unfinishedMessage)
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
                    presenter.loadChatRoomsSuggestions()
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

    // Shows the emoji or the system keyboard.
    private fun toggleKeyboard() {
        if (!emojiKeyboardPopup.isShowing) {
            openEmojiKeyboard()
        } else {
            // If popup is showing, simply dismiss it to show the underlying text keyboard
            dismissEmojiKeyboard()
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
        text_message.asObservable().let {
            compositeDisposable.addAll(
                subscribeComposeButtons(it),
                subscribeComposeTypingStatus(it)
            )
        }
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
        with(activity as ChatRoomActivity) {
            this.clearLightStatusBar()
            this.setupToolbarTitle(toolbarTitle)
            toolbar.isVisible = true
        }
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        text_message?.removeCallbacks(what)
    }

    override fun invalidateDrawable(who: Drawable) {
        text_message?.invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
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

    override fun toggleStar(id: String, star: Boolean) {
        if (star) {
            presenter.starMessage(id)
        } else {
            presenter.unstarMessage(id)
        }
    }

    override fun togglePin(id: String, pin: Boolean) {
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
                .setPositiveButton(it.getString(android.R.string.ok)) { _, _ ->
                    presenter.deleteMessage(
                        roomId,
                        id
                    )
                }
                .setNegativeButton(it.getString(android.R.string.cancel)) { _, _ -> }
                .show()
        }
    }

    override fun copyPermalink(id: String) {
        presenter.copyPermalink(id)
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

    override fun reportMessage(id: String) {
        presenter.reportMessage(
            messageId = id,
            description = "This message was reported by a user from the Android app"
        )
    }

    fun openEmojiKeyboard() {
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
    }

    fun dismissEmojiKeyboard() {
        // Check if the keyboard was ever initialized.
        // It may be the case when you are looking a not joined room
        if (::emojiKeyboardPopup.isInitialized) {
            emojiKeyboardPopup.dismiss()
            setReactionButtonIcon(R.drawable.ic_reaction_24dp)
        }
    }
}
