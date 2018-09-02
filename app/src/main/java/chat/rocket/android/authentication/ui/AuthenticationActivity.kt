package chat.rocket.android.authentication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.domain.model.getLoginDeepLinkInfo
import chat.rocket.android.authentication.onboarding.ui.OnBoardingFragment
import chat.rocket.android.authentication.presentation.AuthenticationPresenter
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var presenter: AuthenticationPresenter
    val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_authentication)
        super.onCreate(savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        launch(UI + job) {
            val newServer = intent.getBooleanExtra(INTENT_ADD_NEW_SERVER, false)
            presenter.loadCredentials(newServer) { authenticated ->
                if (!authenticated) {
                    showOnBoarding()
                }
            }
        }
    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        currentFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    private fun showOnBoarding() {
        addFragment("OnBoardingFragment", R.id.fragment_container, allowStateLoss = true) {
            OnBoardingFragment.newInstance()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.legal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_terms_of_Service -> presenter.termsOfService(getString(R.string.action_terms_of_service))
            R.id.action_privacy_policy -> presenter.privacyPolicy(getString(R.string.action_privacy_policy))
        }
        return super.onOptionsItemSelected(item)
    }
}

const val INTENT_ADD_NEW_SERVER = "INTENT_ADD_NEW_SERVER"

fun Context.newServerIntent(): Intent {
    return Intent(this, AuthenticationActivity::class.java).apply {
        putExtra(INTENT_ADD_NEW_SERVER, true)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}