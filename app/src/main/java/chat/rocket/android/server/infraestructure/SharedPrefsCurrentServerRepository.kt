package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.CurrentServerRepository

class SharedPrefsCurrentServerRepository(private val preferences: SharedPreferences) : CurrentServerRepository {
    private val CURRENT_SERVER = "current_server"

    override fun save(url: String) {
        preferences.edit().putString(CURRENT_SERVER, url).apply()
    }

    override fun get(): String? {
        return preferences.getString(CURRENT_SERVER, null)
    }

}