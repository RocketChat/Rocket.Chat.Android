package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.CurrentServerRepository

class SharedPrefsConnectingServerRepository(private val preferences: SharedPreferences) : CurrentServerRepository {

    override fun save(url: String) {
        preferences.edit().putString(CONNECTING_SERVER_KEY, url).apply()
    }

    override fun get(): String? {
        return preferences.getString(CONNECTING_SERVER_KEY, null)
    }

    companion object {
        private const val CONNECTING_SERVER_KEY = "connecting_server"
    }

    override fun clear() {
        preferences.edit().remove(CONNECTING_SERVER_KEY).apply()
    }
}