package chat.rocket.android.authentication.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationPresenter
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.util.extensions.getDeepLinkInfo
import chat.rocket.android.util.extensions.isDynamicLink
import chat.rocket.android.util.extensions.isSupportedLink
import chat.rocket.common.util.ifNull
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar.*
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: AuthenticationPresenter
    @Inject
    lateinit var dynamicLinksManager: DynamicLinksForFirebase

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        setupToolbar()
        processIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            processIncomingIntent(it)
        }
    }

    private fun processIncomingIntent(intent: Intent) {
        if (intent.isSupportedLink(this)) {
            intent.data?.let { uri ->
                if (uri.isDynamicLink(this)) {
                    resolveDynamicLink(intent)
                } else {
                    uri.getDeepLinkInfo(baseContext)?.let {
                        routeDeepLink(it)
                    }.ifNull {
                        loadCredentials()
                    }
                }
            }
        } else {
            loadCredentials()
        }
    }

    private fun resolveDynamicLink(intent: Intent) {
        val deepLinkCallback = { returnedUri: Uri? ->
            returnedUri?.let {
                returnedUri.getDeepLinkInfo(baseContext)?.let {
                    routeDeepLink(it)
                }
            }.ifNull {
                loadCredentials()
            }
        }
        dynamicLinksManager.getDynamicLink(intent, deepLinkCallback)
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

    private fun routeDeepLink(deepLinkInfo: DeepLinkInfo) {
        presenter.loadCredentials(false) { isAuthenticated ->
            if (isAuthenticated) {
                showChatList(deepLinkInfo)
            } else {
                presenter.saveDeepLinkInfo(deepLinkInfo)
                if (getString(R.string.server_url).isEmpty()) {
                    presenter.toOnBoarding()
                } else {
                    presenter.toSignInToYourServer()
                }
            }
        }
    }

    private fun loadCredentials() {
        val newServer = intent.getBooleanExtra(INTENT_ADD_NEW_SERVER, false)
        presenter.loadCredentials(newServer) { isAuthenticated ->
            when {
                isAuthenticated -> showChatList()
                getString(R.string.server_url).isEmpty() -> presenter.toOnBoarding()
                else -> presenter.toSignInToYourServer()
            }
        }
    }

    private fun showChatList() = presenter.toChatList()

    private fun showChatList(deepLinkInfo: DeepLinkInfo) = presenter.toChatList(deepLinkInfo)
}

const val INTENT_ADD_NEW_SERVER = "INTENT_ADD_NEW_SERVER"

fun Context.newServerIntent(): Intent {
    return Intent(this, AuthenticationActivity::class.java).apply {
        putExtra(INTENT_ADD_NEW_SERVER, true)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}
