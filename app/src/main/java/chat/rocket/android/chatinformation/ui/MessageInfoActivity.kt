package chat.rocket.android.chatinformation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.textContent
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import javax.inject.Inject

fun Context.messageInformationIntent(messageId: String): Intent {
    return Intent(this, MessageInfoActivity::class.java).apply {
        putExtra(INTENT_MESSAGE_ID, messageId)
    }
}

private const val INTENT_MESSAGE_ID = "message_id"

class MessageInfoActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        setupToolbar()

        val messageId = intent.getStringExtra(INTENT_MESSAGE_ID)
        requireNotNull(messageId) { "no message_id provided in Intent extras" }

        if (supportFragmentManager.findFragmentByTag(TAG_MESSAGE_INFO_FRAGMENT) == null) {
            addFragment(TAG_MESSAGE_INFO_FRAGMENT, R.id.fragment_container) {
                newInstance(messageId = messageId)
            }
        }
    }

    private fun setupToolbar() {
        text_toolbar_title.textContent = getString(R.string.message_information_title)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener { finishActivity() }
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.close_enter, R.anim.close_exit)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}