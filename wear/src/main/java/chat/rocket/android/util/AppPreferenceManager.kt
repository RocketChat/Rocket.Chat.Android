package chat.rocket.android.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class AppPreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences?
    private val editor: SharedPreferences.Editor?

    init {
        sharedPreferences = getPreferencesInstanceForServices(context)
        editor = sharedPreferences.edit()
    }

    private fun getPreferencesInstanceForServices(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getSharedPreferenceBoolean(key: String): Boolean {
        return sharedPreferences!!.getBoolean(key, true)
    }

    fun editSharedPreference(key: String, value: Boolean) {
        if (editor != null) {
            editor.putBoolean(key, value)
        }
    }
}
