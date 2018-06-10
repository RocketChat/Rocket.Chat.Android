package chat.rocket.android.util

import android.app.Activity
import android.support.annotation.StringRes
import android.app.Fragment
import android.widget.Toast

fun Activity.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Fragment.showToast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) =
    showToast(getString(resource), duration)

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) =
    activity?.showToast(message, duration)

