package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.ClientCertRepository

class SharedPrefsClientCertRepository(private val preferences: SharedPreferences) : ClientCertRepository {

    override fun save(alias: String) {
        preferences.edit().putString(CLIENT_KEY, alias).apply()
    }

    override fun get(): String? {
        return preferences.getString(CLIENT_KEY, null)
    }

    companion object {
        private const val CLIENT_KEY = ""
    }

    override fun clear() {
        preferences.edit().remove(CLIENT_KEY).apply()
    }
}
