package chat.rocket.android.helper

import android.content.SharedPreferences
import android.preference.PreferenceManager
import chat.rocket.android.app.RocketChatApplication

object SharedPreferenceHelper {
    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(RocketChatApplication.getAppContext())
    private var editor: SharedPreferences.Editor? = sharedPreferences.edit()

    //Add more methods for other types if needed

    fun putInt(key: String, value: Int) {
        editor!!.putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        editor!!.putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun putString(key: String, value: String) {
        editor!!.putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor!!.putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        editor!!.remove(key).apply()
    }
}