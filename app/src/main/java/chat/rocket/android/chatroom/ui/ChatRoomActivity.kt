package chat.rocket.android.chatroom.ui

import DrawableHelper
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
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

fun Context.chatRoomIntent(
    chatRoomId: String,
    chatRoomName: String,
    chatRoomType: String,
    isReadOnly: Boolean,
    chatRoomLastSeen: Long,
    isSubscribed: Boolean = true,
    isCreator: Boolean = false,
    isFavorite: Boolean = false,
    chatRoomMessage: String? = null
): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_NAME, chatRoomName)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        putExtra(INTENT_CHAT_ROOM_IS_READ_ONLY, isReadOnly)
        putExtra(INTENT_CHAT_ROOM_LAST_SEEN, chatRoomLastSeen)
        putExtra(INTENT_CHAT_IS_SUBSCRIBED, isSubscribed)
        putExtra(INTENT_CHAT_ROOM_IS_CREATOR, isCreator)
        putExtra(INTENT_CHAT_ROOM_IS_FAVORITE, isFavorite)
        putExtra(INTENT_CHAT_ROOM_MESSAGE, chatRoomMessage)
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_NAME = "chat_room_name"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"
private const val INTENT_CHAT_ROOM_IS_READ_ONLY = "chat_room_is_read_only"
private const val INTENT_CHAT_ROOM_IS_CREATOR = "chat_room_is_creator"
private const val INTENT_CHAT_ROOM_IS_FAVORITE = "chat_room_is_favorite"
private const val INTENT_CHAT_ROOM_LAST_SEEN = "chat_room_last_seen"
private const val INTENT_CHAT_IS_SUBSCRIBED = "is_chat_room_subscribed"
private const val INTENT_CHAT_ROOM_MESSAGE = "chat_room_message"

class ChatRoomActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    // TODO - workaround for now... We will move to a single activity
    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var navigator: ChatRoomNavigator
    @Inject
    lateinit var managerFactory: ConnectionManagerFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // Workaround for when we are coming to the app via the recents app and the app was killed.
        val serverUrl = serverInteractor.get()
        if (serverUrl != null) {
            managerFactory.create(serverUrl).connect()
        } else {
            navigator.toNewServer()
            return
        }

        val chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        val chatRoomName = intent.getStringExtra(INTENT_CHAT_ROOM_NAME)
        requireNotNull(chatRoomName) { "no chat_room_name provided in Intent extras" }

        val chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
        requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

        val isReadOnly = intent.getBooleanExtra(INTENT_CHAT_ROOM_IS_READ_ONLY, true)

        val isCreator = intent.getBooleanExtra(INTENT_CHAT_ROOM_IS_CREATOR, false)

        val isFavorite = intent.getBooleanExtra(INTENT_CHAT_ROOM_IS_FAVORITE, false)

        val chatRoomLastSeen = intent.getLongExtra(INTENT_CHAT_ROOM_LAST_SEEN, -1)

        val isSubscribed = intent.getBooleanExtra(INTENT_CHAT_IS_SUBSCRIBED, true)

        val chatRoomMessage = intent.getStringExtra(INTENT_CHAT_ROOM_MESSAGE)

        setupToolbar()

        if (supportFragmentManager.findFragmentByTag(TAG_CHAT_ROOM_FRAGMENT) == null) {
            addFragment(TAG_CHAT_ROOM_FRAGMENT, R.id.fragment_container) {
                newInstance(
                    chatRoomId,
                    chatRoomName,
                    chatRoomType,
                    isReadOnly,
                    chatRoomLastSeen,
                    isSubscribed,
                    isCreator,
                    isFavorite,
                    chatRoomMessage
                )
            }
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { finishActivity() }
    }

    fun showToolbarTitle(title: String) {
        text_room_name.textContent = title
    }

    fun showToolbarChatRoomIcon(chatRoomType: String) {
        val drawable = when (roomTypeOf(chatRoomType)) {
            is RoomType.Channel -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_hashtag_black_12dp, this)
            }
            is RoomType.PrivateGroup -> {
                DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_12_dp, this)
            }
            else -> null
        }

        drawable?.let {
            val wrappedDrawable = DrawableHelper.wrapDrawable(it)
            val mutableDrawable = wrappedDrawable.mutate()
            DrawableHelper.tintDrawable(mutableDrawable, this, R.color.colorWhite)
            DrawableHelper.compoundDrawable(text_room_name, mutableDrawable)
        }
    }

    fun hideToolbarChatRoomIcon() {
        text_room_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }
}