package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences

private const val CURRENT_LANGUAGE= "current_language"
class SharedPrefsCurrentLanguageRepository(private val preferences: SharedPreferences) :
    CurrentLanguageRepository {
    override fun save(language: String) {
        preferences.edit().putString(CURRENT_LANGUAGE, language).apply()
    }

    override fun get(): String? {
        return preferences.getString(CURRENT_LANGUAGE, "")
    }
}
