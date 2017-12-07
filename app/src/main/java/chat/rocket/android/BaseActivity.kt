package chat.rocket.android


import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected fun addFragment(tag: String, layoutId: Int, block: (Unit) -> Fragment) {
        val fragment = supportFragmentManager.findFragmentByTag(tag) ?: block(Unit)
        supportFragmentManager.beginTransaction().replace(layoutId, fragment, tag).commit()
    }
}