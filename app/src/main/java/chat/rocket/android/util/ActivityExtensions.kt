package chat.rocket.android.util

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import chat.rocket.android.R

fun AppCompatActivity.addFragment(tag: String, layoutId: Int, newInstance: () -> Fragment) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    supportFragmentManager.beginTransaction()
            .replace(layoutId, fragment, tag)
            .commit()
}

fun AppCompatActivity.addFragmentBackStack(tag: String, layoutId: Int, newInstance: () -> Fragment) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(layoutId, fragment, tag)
            .addToBackStack(tag)
            .commit()
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
}