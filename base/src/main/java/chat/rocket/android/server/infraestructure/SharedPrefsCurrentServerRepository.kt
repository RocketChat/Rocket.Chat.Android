package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.CurrentServerRepository

class SharedPrefsCurrentServerRepository(private val preferences: SharedPreferences) : CurrentServerRepository {

    override fun save(url: String) {
        preferences.edit().putString(CURRENT_SERVER_KEY, url).apply()
    }

    override fun get(): String? {
        return preferences.getString(CURRENT_SERVER_KEY, null)
    }

    companion object {
        private const val CURRENT_SERVER_KEY = "current_server"
    }

    override fun clear() {
        preferences.edit().remove(CURRENT_SERVER_KEY).apply()
    }
}