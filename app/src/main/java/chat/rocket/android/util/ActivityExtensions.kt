package chat.rocket.android.util

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.addFragment(tag: String, layoutId: Int, newInstance: () -> Fragment) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    supportFragmentManager.beginTransaction().replace(layoutId, fragment, tag).commit()
}

fun AppCompatActivity.addFragmentBackStack(tag: String, layoutId: Int, newInstance: () -> Fragment) {
    val fragment = supportFragmentManager.findFragmentByTag(tag) ?: newInstance()
    supportFragmentManager.beginTransaction().replace(layoutId, fragment, tag).addToBackStack(tag).commit()
}