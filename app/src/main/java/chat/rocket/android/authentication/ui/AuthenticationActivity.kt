package chat.rocket.android.authentication.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.app.LayoutHelper
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.util.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    val layoutHelper = LayoutHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_authentication)

        layoutHelper.install(this)
        addFragment("authenticationServerFragment", R.id.fragment_container) {
            ServerFragment.newInstance()
        }
    }

    override fun onDestroy() {
        layoutHelper.remove()
        super.onDestroy()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}