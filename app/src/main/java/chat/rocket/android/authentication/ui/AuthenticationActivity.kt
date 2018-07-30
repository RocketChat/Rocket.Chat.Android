package chat.rocket.android.authentication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.authentication.onboarding.ui.OnBoardingFragment
import chat.rocket.android.authentication.presentation.AuthenticationPresenter
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
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
        setTheme(R.style.AuthenticationTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        launch(UI + job) {
            val newServer = intent.getBooleanExtra(INTENT_ADD_NEW_SERVER, false)
            // if we got authenticateWithDeepLink information, pass true to newServer also
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
        supportFragmentManager.findFragmentById(R.id.fragment_container)?.onActivityResult(requestCode, resultCode, data)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    private fun showOnBoarding() {
        addFragment("OnBoardingFragment", R.id.fragment_container, allowStateLoss = true) {
            OnBoardingFragment.newInstance()
        }
    }
}

const val INTENT_ADD_NEW_SERVER = "INTENT_ADD_NEW_SERVER"

fun Context.newServerIntent(): Intent {
    return Intent(this, AuthenticationActivity::class.java).apply {
        putExtra(INTENT_ADD_NEW_SERVER, true)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}