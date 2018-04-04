package chat.rocket.android.chatroom.ui

import DrawableHelper
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import javax.inject.Inject
import timber.log.Timber


fun Context.chatRoomIntent(chatRoomId: String,
                           chatRoomName: String,
                           chatRoomType: String,
                           isChatRoomReadOnly: Boolean,
                           chatRoomLastSeen: Long,
                           isChatRoomSubscribed: Boolean = true): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_NAME, chatRoomName)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        putExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
        putExtra(INTENT_CHAT_ROOM_LAST_SEEN, chatRoomLastSeen)
        putExtra(INTENT_CHAT_IS_SUBSCRIBED, isChatRoomSubscribed)
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_NAME = "chat_room_name"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"
private const val INTENT_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"
private const val INTENT_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val INTENT_CHAT_IS_SUBSCRIBED = "is_chat_room_subscribed"

class ChatRoomActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    // TODO - workaround for now... We will move to a single activity
    @Inject lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject lateinit var managerFactory: ConnectionManagerFactory

    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var isChatRoomReadOnly: Boolean = false
    private var isChatRoomSubscribed: Boolean = true
    private var chatRoomLastSeen: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // Workaround for when we are coming to the app via the recents app and the app was killed.
        managerFactory.create(serverInteractor.get()!!).connect()

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        chatRoomName = intent.getStringExtra(INTENT_CHAT_ROOM_NAME)
        requireNotNull(chatRoomName) { "no chat_room_name provided in Intent extras" }

        chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
        requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

        isChatRoomReadOnly = intent.getBooleanExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, true)
        requireNotNull(chatRoomType) { "no is_chat_room_read_only provided in Intent extras" }

        setupToolbar()

        chatRoomLastSeen = intent.getLongExtra(INTENT_CHAT_ROOM_LAST_SEEN, -1)

        isChatRoomSubscribed = intent.getBooleanExtra(INTENT_CHAT_IS_SUBSCRIBED, true)

        if (supportFragmentManager.findFragmentByTag("ChatRoomFragment") == null) {
            addFragment("ChatRoomFragment", R.id.fragment_container) {
                newInstance(chatRoomId, chatRoomName, chatRoomType, isChatRoomReadOnly, chatRoomLastSeen,
                        isChatRoomSubscribed)
            }
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
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