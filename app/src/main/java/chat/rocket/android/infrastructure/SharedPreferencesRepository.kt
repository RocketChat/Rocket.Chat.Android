package chat.rocket.android.infrastructure

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesRepository(context: Context) : LocalRepository {

    val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences("local.prefs", Context.MODE_PRIVATE)
    }

    override fun save(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun get(key: String): String? {
        return preferences.getString(key, null)
    }
}