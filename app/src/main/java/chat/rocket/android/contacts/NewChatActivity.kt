package chat.rocket.android.contacts

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.main.ui.onNavDrawerItemSelected
import chat.rocket.android.main.ui.setupMenu
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar.*
import javax.inject.Inject

class NewChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)
        val contactsFragment = ContactsFragment()
        supportFragmentManager!!.beginTransaction().add(R.id.fragment_container, contactsFragment, "contactsFragment").commit();
        setupToolbar()
        setupNavigationView()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupNavigationView() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { finishActivity() }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom)
    }
}
