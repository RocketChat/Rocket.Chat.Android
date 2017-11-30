package chat.rocket.android.app

import android.os.Bundle
import chat.rocket.android.BaseActivity
import chat.rocket.android.R

class AuthenticationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        LayoutHelper.androidBug5497Workaround(this)
        addFragment(AuthenticationSignUpFragment(), "authenticationSignUpFragment", R.id.fragment_container)
    }
}