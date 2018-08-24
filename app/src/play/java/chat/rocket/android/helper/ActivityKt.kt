package chat.rocket.android.helper

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials

fun FragmentActivity.saveCredentials(id: String, password: String) {
    val credentialsClient = Credentials.getClient(this)
    SmartLockHelper.save(credentialsClient, this, id, password)
}

fun Activity.requestStoredCredentials(): Pair<String, String>? {
    val credentialsClient = Credentials.getClient(this)
    return SmartLockHelper.requestStoredCredentials(credentialsClient, this)?.let {
        null
    }
}

fun getCredentials(data: Intent): Pair<String, String>? {
    val credentials: Credential = data.getParcelableExtra(Credential.EXTRA_KEY)
    return credentials.password?.let {
        Pair(credentials.id, it)
    }
}

fun hasCredentialsSupport() = true