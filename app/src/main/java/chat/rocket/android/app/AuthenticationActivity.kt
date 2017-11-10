package chat.rocket.android.app

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import chat.rocket.android.R

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
class AuthenticationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        LayoutHelper.androidBug5497Workaround(this)
        addFragment(AuthenticationLoginFragment(), "authenticationServerFragment")
    }

    private fun addFragment(fragment: Fragment, tag: String) {
        fragmentManager.beginTransaction().add(R.id.fragment_container, fragment, tag).commit()
    }
}