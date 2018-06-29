package chat.rocket.android.main.ui

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import chat.rocket.android.R
import chat.rocket.android.main.presentation.MainPresenter
import chat.rocket.android.main.presentation.MainView
import dagger.android.*
import javax.inject.Inject


class MainActivity : Activity(), MainView, HasActivityInjector,
    HasFragmentInjector {

    @Inject
    lateinit var activityDispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: MainPresenter
    @Inject
    lateinit var navigator: MainNavigator

    private lateinit var navigationDrawer: WearableNavigationDrawerView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_rooms)
        initialiseChatRoomsFragment()
        setUpTopNavigationDrawer()
    }

    override fun activityInjector(): AndroidInjector<Activity> = activityDispatchingAndroidInjector

    override fun fragmentInjector(): DispatchingAndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    private fun initialiseChatRoomsFragment() {
        navigator.addChatRoomsFragment()
    }

    private fun setUpTopNavigationDrawer() {
        navigationDrawer = findViewById(R.id.top_navigation_drawer)
        val mainNavigationAdapter = MainNavigationAdapter(this)
        navigationDrawer.setAdapter(mainNavigationAdapter)
        navigationDrawer.controller.peekDrawer()
        navigationDrawer.addOnItemSelectedListener { pos ->
            when (pos) {
                0 -> navigator.addChatRoomsFragment()

                1 -> navigator.addSettingsFragment()

                2 -> presenter.logout()
            }
        }
    }
}
