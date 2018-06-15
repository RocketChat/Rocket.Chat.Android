package chat.rocket.android.util.extensions

import android.app.Activity
import android.content.Context
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import chat.rocket.android.R

// TODO: Remove. Use KTX instead.
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun ViewGroup.inflate(@LayoutRes resource: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(resource, this, attachToRoot)

fun AppCompatActivity.addFragment(tag: String, layoutId: Int, allowStateLoss: Boolean = false,
                                  newInstance: () -> Fragment) {
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
    if (currentFocus != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            currentFocus.windowToken,
            InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
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

fun RecyclerView.isAtBottom(): Boolean {
    val manager: RecyclerView.LayoutManager? = layoutManager
    if (manager is LinearLayoutManager) {
        return manager.findFirstVisibleItemPosition() == 0
    }

    return false // or true??? we can't determine the first visible item.
}