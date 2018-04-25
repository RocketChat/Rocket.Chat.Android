package chat.rocket.android.infrastructure

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefsLocalRepository(private val preferences: SharedPreferences) : LocalRepository {
    override fun getBoolean(key: String, defValue: Boolean) = preferences.getBoolean(key, defValue)

    override fun getFloat(key: String, defValue: Float) = preferences.getFloat(key, defValue)

    override fun getInt(key: String, defValue: Int) = preferences.getInt(key, defValue)

    override fun getLong(key: String, defValue: Long) = preferences.getLong(key, defValue)

    override fun save(key: String, value: Int) = preferences.edit { putInt(key, value) }

    override fun save(key: String, value: Float) = preferences.edit { putFloat(key, value) }

    override fun save(key: String, value: Long) = preferences.edit { putLong(key, value) }

    override fun save(key: String, value: Boolean) = preferences.edit { putBoolean(key, value) }

    override fun save(key: String, value: String?) = preferences.edit { putString(key, value) }

    override fun get(key: String, defValue: String?): String? = preferences.getString(key, defValue)

    override fun clear(key: String) = preferences.edit { remove(key) }

    override fun clearAllFromServer(server: String) {
        clear(LocalRepository.KEY_PUSH_TOKEN)
        clear(LocalRepository.TOKEN_KEY + server)
        clear(LocalRepository.SETTINGS_KEY + server)
        clear(LocalRepository.CURRENT_USERNAME_KEY)
    }
}