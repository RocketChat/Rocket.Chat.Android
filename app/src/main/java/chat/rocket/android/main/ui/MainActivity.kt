package chat.rocket.android.main.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import chat.rocket.android.R
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.profile.ui.ProfileFragment
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    private var isFragmentAdded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        setupNavigationView()
    }

    override fun onResume() {
        super.onResume()
        if (!isFragmentAdded) {
            // Adding the first fragment.
            addFragment("ChatRoomsFragment")
            isFragmentAdded = true
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.setNavigationOnClickListener {
            drawer_layout.openDrawer(Gravity.START)
        }
    }

    private fun setupNavigationView() {
        view_navigation.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawer_layout.closeDrawer(Gravity.START)
            onNavDrawerItemSelected(menuItem)
            true
        }
    }

    private fun onNavDrawerItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.action_chat_rooms -> {
                addFragment("ChatRoomsFragment", R.id.fragment_container) {
                    ChatRoomsFragment.newInstance()
                }
            }
            R.id.action_profile -> {
                addFragment("ProfileFragment", R.id.fragment_container) {
                    ProfileFragment.newInstance()
                }
            }
        }
    }

    private fun addFragment(tag: String) {
        addFragment(tag, R.id.fragment_container) {
            ChatRoomsFragment.newInstance()
        }
    }
}