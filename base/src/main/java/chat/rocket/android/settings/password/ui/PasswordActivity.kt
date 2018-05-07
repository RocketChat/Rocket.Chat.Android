package chat.rocket.android.settings.password.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_password.*
import javax.inject.Inject

class PasswordActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        setupToolbar()
        addFragment("PasswordFragment")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun addFragment(tag: String) {
        addFragment(tag, R.id.fragment_container) {
            PasswordFragment.newInstance()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        text_change_password.textContent = resources.getString(R.string.title_password)
    }
}
