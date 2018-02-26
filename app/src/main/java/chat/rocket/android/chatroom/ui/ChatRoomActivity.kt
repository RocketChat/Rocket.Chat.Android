package chat.rocket.android.chatroom.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import javax.inject.Inject


fun Context.chatRoomIntent(chatRoomId: String, chatRoomName: String, chatRoomType: String, isChatRoomReadOnly: Boolean): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_NAME, chatRoomName)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        putExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, isChatRoomReadOnly)
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_NAME = "chat_room_name"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"
private const val INTENT_IS_CHAT_ROOM_READ_ONLY = "is_chat_room_read_only"

class ChatRoomActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String
    private var isChatRoomReadOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        chatRoomName = intent.getStringExtra(INTENT_CHAT_ROOM_NAME)
        requireNotNull(chatRoomName) { "no chat_room_name provided in Intent extras" }

        chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
        requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

        isChatRoomReadOnly = intent.getBooleanExtra(INTENT_IS_CHAT_ROOM_READ_ONLY, true)
        requireNotNull(chatRoomType) { "no is_chat_room_read_only provided in Intent extras" }

        setupToolbar(chatRoomName)

        addFragment("ChatRoomFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomName, chatRoomType, isChatRoomReadOnly)
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    private fun setupToolbar(chatRoomName: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        text_room_name.textContent = chatRoomName
        toolbar.setNavigationOnClickListener {
            finishActivity()
        }
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }
}