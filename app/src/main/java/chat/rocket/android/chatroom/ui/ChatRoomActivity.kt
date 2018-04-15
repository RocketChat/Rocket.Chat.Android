package chat.rocket.android.chatroom.ui

import DrawableHelper
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.ObserveChatRoomsInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.widget.roomupdate.UpdateObserver
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import javax.inject.Inject


fun Context.chatRoomIntent(chatRoomId: String,
                           chatRoomName: String,
                           chatRoomType: String,
                           isChatRoomReadOnly: Boolean,
                           chatRoomLastSeen: Long,
                           chatRoomUpdatedAt: Long?,
                           isChatRoomSubscribed: Boolean = true): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_NAME, chatRoomName)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        putExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
        putExtra(INTENT_CHAT_ROOM_LAST_SEEN, chatRoomLastSeen)
        putExtra(INTENT_CHAT_ROOM_UPDATED_AT, chatRoomUpdatedAt)
        putExtra(INTENT_CHAT_IS_SUBSCRIBED, isChatRoomSubscribed)
    }
}

private const val CHAT_ROOM_FRAGMENT_TAG = "ChatRoomFragment"
private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_NAME = "chat_room_name"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"
private const val INTENT_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"
private const val INTENT_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val INTENT_CHAT_ROOM_UPDATED_AT = "chat_room_updated_at"
private const val INTENT_CHAT_IS_SUBSCRIBED = "is_chat_room_subscribed"

class ChatRoomActivity : AppCompatActivity(), HasSupportFragmentInjector, UpdateObserver {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var observeChatRooms: ObserveChatRoomsInteractor

    // TODO - workaround for now... We will move to a single activity
    @Inject lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject lateinit var managerFactory: ConnectionManagerFactory

    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var isChatRoomReadOnly: Boolean = false
    private var isChatRoomSubscribed: Boolean = true
    private var chatRoomUpdatedAt: Long = -1L
    private var chatRoomLastSeen: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // Workaround for when we are coming to the app via the recents app and the app was killed.
        managerFactory.create(serverInteractor.get()!!).connect()

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        if (savedInstanceState != null) {
            chatRoomName = savedInstanceState.getString(INTENT_CHAT_ROOM_NAME)
            chatRoomType = savedInstanceState.getString(INTENT_CHAT_ROOM_TYPE)
            isChatRoomReadOnly = savedInstanceState.getBoolean(INTENT_IS_CHAT_ROOM_READ_ONLY)
            chatRoomUpdatedAt = savedInstanceState.getLong(INTENT_CHAT_ROOM_UPDATED_AT)
        }
        else {
            chatRoomName = intent.getStringExtra(INTENT_CHAT_ROOM_NAME)
            requireNotNull(chatRoomName) { "no chat_room_name provided in Intent extras" }

            chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
            requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

            isChatRoomReadOnly = intent.getBooleanExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, true)
            requireNotNull(chatRoomType) { "no is_chat_room_read_only provided in Intent extras" }
        }

        setupToolbar()

        chatRoomLastSeen = intent.getLongExtra(INTENT_CHAT_ROOM_LAST_SEEN, -1)

        isChatRoomSubscribed = intent.getBooleanExtra(INTENT_CHAT_IS_SUBSCRIBED, true)

        chatRoomUpdatedAt = intent.getLongExtra(INTENT_CHAT_ROOM_UPDATED_AT, -1L)

        if (isChatRoomSubscribed)
            observeChatRooms.registerObserver(this)

        if (supportFragmentManager.findFragmentByTag(CHAT_ROOM_FRAGMENT_TAG) == null) {
            addFragment(CHAT_ROOM_FRAGMENT_TAG, R.id.fragment_container) {
                newInstance(chatRoomId, chatRoomName, chatRoomType, isChatRoomReadOnly, chatRoomLastSeen,
                        isChatRoomSubscribed)
            }
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(INTENT_CHAT_ROOM_NAME, chatRoomName)
        outState.putString(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        outState.putLong(INTENT_CHAT_ROOM_UPDATED_AT, chatRoomUpdatedAt)
        outState.putBoolean(INTENT_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
        super.onSaveInstanceState(outState)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    override fun onDestroy() {
        super.onDestroy()

        observeChatRooms.removeObserver(this)
    }

    override fun provideRoomId(): String {
        return chatRoomId
    }

    override fun lastUpdated(): Long {
        return chatRoomUpdatedAt
    }

    override fun onRoomChanged(name: String, type: RoomType, readOnly: Boolean?, updatedAt: Long?) {
        val fragment: Fragment = supportFragmentManager.findFragmentByTag(CHAT_ROOM_FRAGMENT_TAG)

        chatRoomName = name
        chatRoomType = type.toString()
        isChatRoomReadOnly = readOnly ?: false
        chatRoomUpdatedAt = updatedAt ?: -1L

        (fragment as ChatRoomFragment).onRoomUpdated(chatRoomName, type, isChatRoomReadOnly)
    }

    fun showRoomTypeIcon(showRoomTypeIcon: Boolean) {
        if (showRoomTypeIcon) {
            val roomType = roomTypeOf(chatRoomType)
            val drawable = when (roomType) {
                is RoomType.Channel -> {
                    DrawableHelper.getDrawableFromId(R.drawable.ic_room_channel, this)
                }
                is RoomType.PrivateGroup -> {
                    DrawableHelper.getDrawableFromId(R.drawable.ic_room_lock, this)
                }
                is RoomType.DirectMessage -> {
                    DrawableHelper.getDrawableFromId(R.drawable.ic_room_dm, this)
                }
                else -> null
            }

            drawable?.let {
                val wrappedDrawable = DrawableHelper.wrapDrawable(it)
                val mutableDrawable = wrappedDrawable.mutate()
                DrawableHelper.tintDrawable(mutableDrawable, this, R.color.white)
                DrawableHelper.compoundDrawable(text_room_name, mutableDrawable)
            }
        } else {
            text_room_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        text_room_name.textContent = chatRoomName

        showRoomTypeIcon(true)

        toolbar.setNavigationOnClickListener {
            finishActivity()
        }
    }

    fun setupToolbarTitle(toolbarTitle: String) {
        text_room_name.textContent = toolbarTitle
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }
}