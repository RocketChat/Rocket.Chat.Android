package chat.rocket.android.util

import android.app.Activity
import android.app.Fragment
import android.widget.Toast
import androidx.annotation.StringRes

fun Activity.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Fragment.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    activity?.showToast(message, duration)

fun Activity.addFragment(tag: String, layoutId: Int, newInstance: () -> Fragment) {
    val fragment = fragmentManager.findFragmentByTag(tag) ?: newInstance()
    fragmentManager.beginTransaction()
        .replace(layoutId, fragment)
        .commit()
}

fun Activity.addFragmentBackStack(
    tag: String,
    layoutId: Int,
    newInstance: () -> Fragment
) {
    val fragment = fragmentManager.findFragmentByTag(tag) ?: newInstance()
    fragmentManager.beginTransaction()
        .replace(layoutId, fragment, tag)
        .addToBackStack(tag)
        .commit()
}