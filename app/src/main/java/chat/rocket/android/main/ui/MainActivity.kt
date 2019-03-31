package chat.rocket.android.main.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.presentation.MainView
import chat.rocket.android.push.refreshPushToken
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.ui.INTENT_CHAT_ROOM_ID
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.invalidateFirebaseToken
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView, HasActivityInjector,
    HasSupportFragmentInjector {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fagmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: MainPresenter
    private var chatRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshPushToken()
        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)

        with(presenter) {
            clearNotificationsForChatRoom(chatRoomId)
            connect()
            getCurrentServerName()
            getAllServers()
            showChatList(chatRoomId)
        }

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        clearAppNotifications()
    }

    override fun activityInjector(): AndroidInjector<Activity> =
        activityDispatchingAndroidInjector

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fagmentDispatchingAndroidInjector

    override fun setupToolbar(serverName: String) {
        setSupportActionBar(toolbar)
        text_server_name.text = serverName
    }

    override fun setupServerListView(serverList: List<Account>) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun invalidateToken(token: String) = invalidateFirebaseToken(token)

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showMessage(message: String) = showToast(message)

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    private fun clearAppNotifications() =
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

    private fun setupListeners() {
        text_server_name.setOnClickListener {
//            SortByBottomSheetFragment().show(supportFragmentManager, TAG)
        }
    }

}
