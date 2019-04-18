package chat.rocket.android.main.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.chatrooms.ui.TAG_CHAT_ROOMS_FRAGMENT
import chat.rocket.android.helper.Constants
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.push.refreshPushToken
import chat.rocket.android.server.ui.INTENT_CHAT_ROOM_ID
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasActivityInjector,
    HasSupportFragmentInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fagmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: MainPresenter
    private var deepLinkInfo: DeepLinkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshPushToken()
        deepLinkInfo = intent.getParcelableExtra(Constants.DEEP_LINK_INFO)

        with(presenter) {
            connect()
            clearNotificationsForChatRoom(intent.getStringExtra(INTENT_CHAT_ROOM_ID))
            showChatList(intent.getStringExtra(INTENT_CHAT_ROOM_ID), deepLinkInfo)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            var deepLinkInfo = it.getParcelableExtra<DeepLinkInfo>(Constants.DEEP_LINK_INFO)
            if (deepLinkInfo != null) {
                val chatRoomsFragment = supportFragmentManager.findFragmentByTag(TAG_CHAT_ROOMS_FRAGMENT) as ChatRoomsFragment
                chatRoomsFragment?.let {
                    it.processDeepLink(deepLinkInfo)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        clearAppNotifications()
    }

    override fun activityInjector(): AndroidInjector<Activity> =
        activityDispatchingAndroidInjector

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fagmentDispatchingAndroidInjector

    private fun clearAppNotifications() =
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
}
