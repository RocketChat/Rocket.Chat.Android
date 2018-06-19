package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.view.*
import androidx.core.text.bold
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.chatroom.adapter.*
import chat.rocket.android.chatroom.presentation.ChatRoomPresenter
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.helper.EndlessRecyclerViewScrollListener
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.extensions.asObservable
import chat.rocket.android.util.extensions.circularRevealOrUnreveal
import chat.rocket.android.util.extensions.fadeIn
import chat.rocket.android.util.extensions.fadeOut
import chat.rocket.android.util.extensions.hideKeyboard
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.rotateBy
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.widget.emoji.*
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.model.ChatRoom
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
    isChatRoomReadOnly: Boolean,
    chatRoomLastSeen: Long,
    isSubscribed: Boolean = true,
    isChatRoomCreator: Boolean = false,
    chatRoomMessage: String? = null
): Fragment {
    return ChatRoomFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_NAME, chatRoomName)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
            putBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
            putLong(BUNDLE_CHAT_ROOM_LAST_SEEN, chatRoomLastSeen)
            putBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED, isSubscribed)
            putBoolean(BUNDLE_CHAT_ROOM_IS_CREATOR, isChatRoomCreator)
            putString(BUNDLE_CHAT_ROOM_MESSAGE, chatRoomMessage)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_NAME = "chat_room_name"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"
private const val BUNDLE_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"
private const val REQUEST_CODE_FOR_PERFORM_SAF = 42
private const val BUNDLE_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val BUNDLE_CHAT_ROOM_IS_SUBSCRIBED = "chat_room_is_subscribed"
private const val BUNDLE_CHAT_ROOM_IS_CREATOR = "chat_room_is_creator"
private const val BUNDLE_CHAT_ROOM_MESSAGE = "chat_room_message"

class ChatRoomFragment : Fragment(), ChatRoomView, EmojiKeyboardListener, EmojiReactionListener {

    @Inject
    lateinit var presenter: ChatRoomPresenter
    @Inject
    lateinit var parser: MessageParser
    private lateinit var adapter: ChatRoomAdapter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var chatRoomMessage: String? = null
    private var isSubscribed: Boolean = true
    private var isChatRoomReadOnly: Boolean = false
    private var isChatRoomCreator: Boolean = false
    private var isBroadcastChannel: Boolean = false
    private lateinit var emojiKeyboardPopup: EmojiKeyboardPopup
    private var chatRoomLastSeen: Long = -1
    private lateinit var actionSnackbar: ActionSnackbar
    private var citation: String? = null
    private var editingMessageId: String? = null

    private val compositeDisposable = CompositeDisposable()
    private var playComposeMessageButtonsAnimation = true

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)

        val bundle = arguments
        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomName = bundle.getString(BUNDLE_CHAT_ROOM_NAME)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
            isChatRoomReadOnly = bundle.getBoolean(BUNDLE_IS_CHAT_ROOM_READ_ONLY)
            isSubscribed = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_SUBSCRIBED)
            chatRoomLastSeen = bundle.getLong(BUNDLE_CHAT_ROOM_LAST_SEEN)
            isChatRoomCreator = bundle.getBoolean(BUNDLE_CHAT_ROOM_IS_CREATOR)
            chatRoomMessage = bundle.getString(BUNDLE_CHAT_ROOM_MESSAGE)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
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
        handler.removeCallbacksAndMessages(null)
        unsubscribeComposeTextMessage()

        // Hides the keyboard (if it's opened) before going to any view.
        activity?.apply {
            hideKeyboard()
        }
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
        menu.findItem(R.id.action_members_list)?.isVisible = !isBroadcastChannel
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members_list -> {
                presenter.toMembersList(chatRoomId)
            }
            R.id.action_pinned_messages -> {
                presenter.toPinnedMessageList(chatRoomId)
            }
            R.id.action_favorite_messages -> {
                presenter.toFavoriteMessageList(chatRoomId)
            }
            R.id.action_files -> {
                presenter.toFileList(chatRoomId)
            }
        }
        return true
    }

    override fun showMessages(dataSet: List<BaseUiModel<*>>) {
        ui {
            // track the message sent immediately after the current message
            var prevMessageUiModel: MessageUiModel? = null

            // Loop over received messages to determine first unread
            for (i in dataSet.indices) {
                val msgModel = dataSet[i]

                if (msgModel is MessageUiModel) {
                    val msg = msgModel.rawData
                    if (msg.timestamp < chatRoomLastSeen) {
                        // This message was sent before the last seen of the room. Hence, it was seen.
                        // if there is a message after (below) this, mark it firstUnread.
                        if (prevMessageUiModel != null) {
                            prevMessageUiModel.isFirstUnread = true
                        }
                        break
                    }
                    prevMessageUiModel = msgModel
                }
            }

            if (recycler_view.adapter == null) {
                adapter = ChatRoomAdapter(
                    chatRoomType,
                    chatRoomName,
                    presenter,
                    reactionListener = this@ChatRoomFragment,
                    context = context
                )
                recycler_view.adapter = adapter
                if (dataSet.size >= 30) {
                    recycler_view.addOnScrollListener(endlessRecyclerViewScrollListener)
                }
                recycler_view.addOnLayoutChangeListener(layoutChangeListener)
                recycler_view.addOnScrollListener(onScrollListener)
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
            if (isBroadcastChannel && !userCanMod) activity?.invalidateOptionsMenu()
        }
    }

    override fun openDirectMessage(chatRoom: ChatRoom, permalink: String) {

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
                button_fab.hide()
            } else {
                if (dy < 0 && !button_fab.isVisible) {
                    button_fab.show()
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
                1 -> {
                    text_typing_status.text =
                        SpannableStringBuilder()
                            .bold { append(usernameList[0]) }
                            .append(getString(R.string.msg_is_typing))
                }
                2 -> {
                    text_typing_status.text =
                        SpannableStringBuilder()
                            .bold { append(usernameList[0]) }
                            .append(getString(R.string.msg_and))
                            .bold { append(usernameList[1]) }
                            .append(getString(R.string.msg_are_typing))
                }
                else -> {
                    text_typing_status.text = getString(R.string.msg_several_users_are_typing)
                }
            }
            text_typing_status.isVisible = true
        }
    }

    override fun hideTypingStatusView() {
        ui {
            text_typing_status.isVisible = false
        }
    }

    override fun uploadFile(uri: Uri) {
        // TODO Just leaving a blank message that comes with the file for now. In the future lets add the possibility to add a message with the file to be uploaded.
        presenter.uploadFile(chatRoomId, uri, "")
    }

    override fun showInvalidFileMessage() {
        showMessage(getString(R.string.msg_invalid_file))
    }

    override fun showNewMessage(message: List<BaseUiModel<*>>) {
        ui {
            adapter.prependData(message)
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
            clearMessageComposition()
        }
    }


    override fun clearMessageComposition() {
        ui {
            citation = null
            editingMessageId = null
            text_message.textContent = ""
            actionSnackbar.dismiss()
        }
    }

    override fun dispatchUpdateMessage(index: Int, message: List<BaseUiModel<*>>) {
        ui {
            adapter.updateItem(message.last())
            if (message.size > 1) {
                adapter.prependData(listOf(message.first()))
            }
        }
    }

    override fun dispatchDeleteMessage(msgId: String) {
        ui {
            adapter.removeItem(msgId)
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
            text_message.text?.insert(cursorPosition, EmojiParser.parse(emoji.shortname))
            text_message.setSelection(cursorPosition + emoji.unicode.length)
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
            emojiPickerPopup.listener = object : EmojiListenerAdapter() {
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
            connection_status_text.fadeIn()
            handler.removeCallbacks(dismissStatus)
            when (state) {
                is State.Connected -> {
                    connection_status_text.text = getString(R.string.status_connected)
                    handler.postDelayed(dismissStatus, 2000)
                }
                is State.Disconnected -> connection_status_text.text =
                        getString(R.string.status_disconnected)
                is State.Connecting -> connection_status_text.text =
                        getString(R.string.status_connecting)
                is State.Authenticating -> connection_status_text.text =
                        getString(R.string.status_authenticating)
                is State.Disconnecting -> connection_status_text.text =
                        getString(R.string.status_disconnecting)
                is State.Waiting -> connection_status_text.text =
                        getString(R.string.status_waiting, state.seconds)
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
        connection_status_text.fadeOut()
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
            button_fab.hide()
        }
    }

    private fun setupMessageComposer(canPost: Boolean) {
        if (isChatRoomReadOnly && !canPost) {
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

            subscribeComposeTextMessage()
            emojiKeyboardPopup =
                    EmojiKeyboardPopup(activity!!, activity!!.findViewById(R.id.fragment_container))
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

    private fun setupSuggestionsView() {
        suggestions_view.anchorTo(text_message)
            .setMaximumHeight(resources.getDimensionPixelSize(R.dimen.suggestions_box_max_height))
            .addTokenAdapter(PeopleSuggestionsAdapter(context!!))
            .addTokenAdapter(CommandSuggestionsAdapter())
            .addTokenAdapter(RoomSuggestionsAdapter())
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
            .addSuggestionProviderAction("/") { _ ->
                presenter.loadCommands()
            }

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
        actionSnackbar.cancelView.setOnClickListener({
            clearMessageComposition()
            KeyboardHelper.showSoftKeyboard(text_message)
        })
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
}