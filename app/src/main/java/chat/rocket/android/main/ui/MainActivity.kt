package chat.rocket.android.main.ui

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.chatrooms.ui.TAG_CHAT_ROOMS_FRAGMENT
import chat.rocket.android.core.behaviours.AppLanguageView
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.server.ui.INTENT_CHAT_ROOM_ID
import chat.rocket.android.authentication.domain.model.DEEP_LINK_INFO_KEY
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasActivityInjector,
    HasSupportFragmentInjector, AppLanguageView {
    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: MainPresenter
    private var deepLinkInfo: DeepLinkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deepLinkInfo = intent.getParcelableExtra(DEEP_LINK_INFO_KEY)

        with(presenter) {
            connect()
            getAppLanguage()
            removeOldAccount()
            saveNewAccount()
            registerPushNotificationToken()
            intent.getStringExtra(INTENT_CHAT_ROOM_ID).let {
                clearNotificationsForChatRoom(it)
                showChatList(it, deepLinkInfo)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getParcelableExtra<DeepLinkInfo>(DEEP_LINK_INFO_KEY)?.let { deepLinkInfo ->
            (supportFragmentManager.findFragmentByTag(TAG_CHAT_ROOMS_FRAGMENT) as? ChatRoomsFragment)
                ?.processDeepLink(deepLinkInfo)
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

    override fun updateLanguage(language: String, country: String?) {
        val locale: Locale = if (country != null) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        Locale.setDefault(locale)

        val config = Configuration()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            config.locale = locale
        }

        // TODO We need to check out a better way to use createConfigurationContext
        // instead of updateConfiguration here since it is deprecated.
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun clearAppNotifications() =
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
}
