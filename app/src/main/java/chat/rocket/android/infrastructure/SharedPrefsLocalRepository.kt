package chat.rocket.android.infrastructure

import android.content.SharedPreferences

class SharedPrefsLocalRepository(private val preferences: SharedPreferences) : LocalRepository {

    override fun save(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun get(key: String): String? {
        return preferences.getString(key, null)
    }

    override fun clear(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun clearAllFromServer(server: String) {
        clear(LocalRepository.KEY_PUSH_TOKEN)
        clear(LocalRepository.TOKEN_KEY + server)
        clear(LocalRepository.SETTINGS_KEY + server)
    }
}