package chat.rocket.android.infrastructure

import android.content.SharedPreferences

class SharedPrefsLocalRepository(private val preferences: SharedPreferences) : LocalRepository {
    override fun getBoolean(key: String) = preferences.getBoolean(key, false)

    override fun getFloat(key: String) = preferences.getFloat(key, -1f)

    override fun getInt(key: String) = preferences.getInt(key, -1)

    override fun getLong(key: String) = preferences.getLong(key, -1L)

    override fun save(key: String, value: Int) = preferences.edit().putInt(key, value).apply()

    override fun save(key: String, value: Float) = preferences.edit().putFloat(key, value).apply()

    override fun save(key: String, value: Long) = preferences.edit().putLong(key, value).apply()

    override fun save(key: String, value: Boolean) = preferences.edit().putBoolean(key, value).apply()

    override fun save(key: String, value: String?) = preferences.edit().putString(key, value).apply()

    override fun get(key: String): String? = preferences.getString(key, null)

    override fun clear(key: String) = preferences.edit().remove(key).apply()

    override fun clearAllFromServer(server: String) {
        clear(LocalRepository.KEY_PUSH_TOKEN)
        clear(LocalRepository.TOKEN_KEY + server)
        clear(LocalRepository.SETTINGS_KEY + server)
        clear(LocalRepository.CURRENT_USERNAME_KEY)
    }
}