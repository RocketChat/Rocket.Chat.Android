package chat.rocket.android.authentication.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.helper.LayoutHelper
import chat.rocket.android.util.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class AuthenticationActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    private val layoutHelper = LayoutHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        AndroidInjection.inject(this)
        layoutHelper.install(this)

        addFragment("authenticationServerFragment", R.id.fragment_container) {
            ServerFragment.newInstance()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        layoutHelper.remove()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}