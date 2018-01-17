package chat.rocket.android.authentication.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.authentication.presentation.AuthenticationPresenter
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.util.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject lateinit var presenter: AuthenticationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        presenter.loadCredentials { authenticated ->
            if (authenticated) {
                // just call onCreate, and the presenter will call the navigator...
                super.onCreate(savedInstanceState)
            } else {
                showServerInput(savedInstanceState)
            }
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }

    fun showServerInput(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_authentication)
        setTheme(R.style.AuthenticationTheme)

        super.onCreate(savedInstanceState)

        addFragment("ServerFragment", R.id.fragment_container) {
            ServerFragment.newInstance()
        }
    }
}