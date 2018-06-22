package chat.rocket.android.chatroom.ui

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import chat.rocket.android.R
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import dagger.android.*
import javax.inject.Inject

//call this when quitting main activity
fun Context.chatRoomIntent(
    chatRoomId: String,
    chatRoomName: String,
    chatRoomType: String
): Intent {
    return Intent(this, ChatRoomActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_NAME, chatRoomName)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_NAME = "chat_room_name"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"

class ChatRoomActivity : Activity(), HasActivityInjector, HasFragmentInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var navigator: ChatRoomNavigator

    private lateinit var chatRoomId: String
    private lateinit var chatRoomName: String
    private lateinit var chatRoomType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        getValuesFromIntent()
        navigator.toChatRoom(chatRoomId, chatRoomName, chatRoomType)
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    override fun fragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun getValuesFromIntent() {
        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        chatRoomName = intent.getStringExtra(INTENT_CHAT_ROOM_NAME)
        chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
    }
}