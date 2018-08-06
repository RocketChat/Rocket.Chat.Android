package chat.rocket.android.helper

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.saveCredentials(id: String, password: String) {
}

fun Activity.requestStoredCredentials(): Pair<String, String>? = null

fun getCredentials(data: Intent): Pair<String, String>? = null

fun hasCredentialsSupport() = false