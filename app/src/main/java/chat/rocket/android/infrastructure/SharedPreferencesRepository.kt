package chat.rocket.android.infrastructure

import android.content.SharedPreferences

class SharedPreferencesRepository(private val preferences: SharedPreferences) : LocalRepository {

    override fun save(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun get(key: String): String? {
        return preferences.getString(key, null)
    }
}