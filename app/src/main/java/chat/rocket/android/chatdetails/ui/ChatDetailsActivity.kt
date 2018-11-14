package chat.rocket.android.chatdetails.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_details.*
import javax.inject.Inject

fun Context.chatDetailsIntent(
        chatRoomId: String,
        chatRoomType: String,
        isSubscribed: Boolean = true,
        isMenuDisabled: Boolean = false
): Intent {
    return Intent(this, ChatDetailsActivity::class.java).apply {
        putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)
        putExtra(INTENT_CHAT_IS_SUBSCRIBED, isSubscribed)
        putExtra(INTENT_CHAT_DISABLED_MENU, isMenuDisabled)
    }
}

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"
private const val INTENT_CHAT_IS_SUBSCRIBED = "is_chat_room_subscribed"
private const val INTENT_CHAT_DISABLED_MENU = "is_menu_disabled"

class ChatDetailsActivity: AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        setupToolbar()

        val chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        val chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
        requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

        val isSubscribed = intent.getBooleanExtra(INTENT_CHAT_IS_SUBSCRIBED, true)
        val disableMenu = intent.getBooleanExtra(INTENT_CHAT_DISABLED_MENU, false)

        if (supportFragmentManager.findFragmentByTag(TAG_CHAT_DETAILS_FRAGMENT) == null) {
            addFragment(TAG_CHAT_DETAILS_FRAGMENT, R.id.fragment_container) {
                newInstance(chatRoomId, chatRoomType, isSubscribed, disableMenu)
            }
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
            fragmentDispatchingAndroidInjector

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }

    fun setNavigationIcon(resource: Int) {
        toolbar.setNavigationIcon(resource)
    }

    fun setToolbarTitle(title: String) {
        toolbar_title.text = title
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setToolbarTitle(getString(R.string.title_channel_details))
        setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}