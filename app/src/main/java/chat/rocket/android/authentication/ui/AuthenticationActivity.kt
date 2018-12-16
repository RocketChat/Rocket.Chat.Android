package chat.rocket.android.authentication.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.domain.model.getLoginDeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationPresenter
import chat.rocket.android.helper.Constants
import chat.rocket.android.helper.SharedPreferenceHelper
import chat.rocket.android.util.TimberLogger
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.common.util.ifNull
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.coroutines.experimental.Job
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: AuthenticationPresenter
    val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        setupToolbar()
        loadCredentials()
        getDynamicLink()
    }

    private fun setupToolbar() {
        with(toolbar) {
            setSupportActionBar(this)
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener { onBackPressed() }
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> =
        fragmentDispatchingAndroidInjector

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.legal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_terms_of_Service -> presenter.termsOfService(getString(R.string.action_terms_of_service))
            R.id.action_privacy_policy -> presenter.privacyPolicy(getString(R.string.action_privacy_policy))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadCredentials() {
        intent.getLoginDeepLinkInfo()?.let {
            showServerFragment(it)
        }.ifNull {
            val newServer = intent.getBooleanExtra(INTENT_ADD_NEW_SERVER, false)
            presenter.loadCredentials(newServer) { isAuthenticated ->
                if (isAuthenticated) {
                    showChatList()
                } else {
                    showOnBoardingFragment()
                }
            }
        }
    }

    private fun showOnBoardingFragment() {
        addFragment(
            ScreenViewEvent.OnBoarding.screenName,
            R.id.fragment_container,
            allowStateLoss = true
        ) {
            chat.rocket.android.authentication.onboarding.ui.newInstance()
        }
    }

    private fun showServerFragment(deepLinkInfo: LoginDeepLinkInfo) {
        addFragment(
            ScreenViewEvent.Server.screenName,
            R.id.fragment_container,
            allowStateLoss = true
        ) {
            chat.rocket.android.authentication.server.ui.newInstance()
        }
    }

    private fun showChatList() = presenter.toChatList()

	private fun getDynamicLink() {
		FirebaseDynamicLinks.getInstance()
			.getDynamicLink(intent)
			.addOnSuccessListener(this) { pendingDynamicLinkData ->
				var deepLink: Uri? = null
				if (pendingDynamicLinkData != null) {
					deepLink = pendingDynamicLinkData.link
				}

				TimberLogger.debug("DeepLink : deepLink.toString()")
				SharedPreferenceHelper.putString(Constants.DEEP_LINK, deepLink.toString())
			}
			.addOnFailureListener(this) { e -> TimberLogger.debug("getDynamicLink:onFailure : $e") }
	}
}

const val INTENT_ADD_NEW_SERVER = "INTENT_ADD_NEW_SERVER"

fun Context.newServerIntent(): Intent {
    return Intent(this, AuthenticationActivity::class.java).apply {
        putExtra(INTENT_ADD_NEW_SERVER, true)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}