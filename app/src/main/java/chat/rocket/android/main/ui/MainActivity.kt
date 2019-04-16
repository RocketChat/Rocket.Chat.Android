package chat.rocket.android.main.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.push.refreshPushToken
import chat.rocket.android.server.ui.INTENT_CHAT_ROOM_ID
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasActivityInjector, HasSupportFragmentInjector {
    @Inject lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshPushToken()

        with(presenter) {
            connect()
            intent.getStringExtra(INTENT_CHAT_ROOM_ID).let {
                clearNotificationsForChatRoom(it)
                showChatList(it)
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
        fragmentDispatchingAndroidInjector

    private fun clearAppNotifications() =
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
}
