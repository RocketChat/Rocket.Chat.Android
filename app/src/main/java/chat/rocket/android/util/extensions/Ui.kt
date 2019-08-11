package chat.rocket.android.util.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import chat.rocket.android.R
import chat.rocket.android.thememanager.util.ThemeUtil

fun FragmentActivity.setInvisibleStatusBar(view: View, @ColorInt color: Int = 0) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val isDark = ThemeUtil.getIsDark(applicationContext)
        var flags = view.systemUiVisibility
        window.decorView.systemUiVisibility = (if (isDark){
            flags and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()}else{
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR})
        window.statusBarColor = if (color == 0) {
            ThemeUtil.getThemeColor(android.R.attr.colorBackground)
        } else {
            color
        }
    }
}

fun FragmentActivity.clearInvisibleStatusBar(view: View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val flags = view.systemUiVisibility
        window.decorView.systemUiVisibility = flags and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
        window.statusBarColor = ThemeUtil.getThemeColor(R.attr.colorPrimaryDark)
    }
}

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(resource, this, attachToRoot)

fun AppCompatActivity.addFragment(
    tag: String,
    layoutId: Int,
    allowStateLoss: Boolean = false,
    newInstance: () -> Fragment
) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    val transaction = supportFragmentManager.beginTransaction()
        .replace(layoutId, fragment, tag)
    if (allowStateLoss) {
        transaction.commitAllowingStateLoss()
    } else {
        transaction.commit()
    }
}

fun AppCompatActivity.addFragmentBackStack(
    tag: String,
    layoutId: Int,
    newInstance: () -> Fragment
) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.enter_from_right, R.anim.exit_to_left,
            R.anim.enter_from_left, R.anim.exit_to_right
        )
        .replace(layoutId, fragment, tag)
        .addToBackStack(tag)
        .commit()
}

fun AppCompatActivity.toPreviousView() {
    supportFragmentManager.popBackStack()
}

fun Activity.hideKeyboard() {
    currentFocus?.run {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).also {
            it.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }
    }
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN)
}

fun Activity.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Fragment.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    activity?.showToast(message, duration)

@SuppressLint("RestrictedApi")
fun Context.inflate(@MenuRes menuRes: Int): Menu {
    val menu = MenuBuilder(this)
    val menuInflater = SupportMenuInflater(this)
    menuInflater.inflate(menuRes, menu)
    return menu
}

/**
 * Developed by Magora-Systems.com
 * @since 2017
 * @author Anton Vlasov - whalemare
 */
fun Menu.toList(): List<MenuItem> {
    val menuItems = ArrayList<MenuItem>(this.size())
    (0 until this.size()).mapTo(menuItems) { this.getItem(it) }
    return menuItems
}
