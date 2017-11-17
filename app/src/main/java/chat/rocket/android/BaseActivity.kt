package chat.rocket.android

import android.app.Activity
import android.app.Fragment

abstract class BaseActivity : Activity() {

    protected fun addFragment(fragment: Fragment, tag: String, layoutId: Int) {
        fragmentManager.beginTransaction().add(layoutId, fragment, tag).commit()
    }
}