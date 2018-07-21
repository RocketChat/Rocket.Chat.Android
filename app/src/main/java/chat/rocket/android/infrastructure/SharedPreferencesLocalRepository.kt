package chat.rocket.android.infrastructure

import android.content.SharedPreferences
import androidx.core.content.edit
import chat.rocket.common.model.User
import com.squareup.moshi.Moshi

class SharedPreferencesLocalRepository(
    private val preferences: SharedPreferences,
    moshi: Moshi
) : LocalRepository {

    private val userAdapter = moshi.adapter(User::class.java)

    override fun getCurrentUser(url: String): User? {
        return get("${url}_${LocalRepository.USER_KEY}", null)?.let {
            userAdapter.fromJson(it)
        }
    }

    override fun saveCurrentUser(url: String, user: User) {
        save("${url}_${LocalRepository.USER_KEY}", userAdapter.toJson(user))
    }

    override fun saveLastChatroomsRefresh(url: String, timestamp: Long) {
        save("$url${LocalRepository.LAST_CHATROOMS_REFRESH}", timestamp)
    }

    override fun getLastChatroomsRefresh(url: String) =
        getLong("$url${LocalRepository.LAST_CHATROOMS_REFRESH}", 0L)

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
        preferences.all.keys.forEach { key ->
            if (key.startsWith(server, ignoreCase = true)) {
                clear(key)
            }
        }
    }
}
