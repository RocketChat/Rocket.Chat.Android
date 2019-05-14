package chat.rocket.android.server.infrastructure

import android.content.SharedPreferences
import java.util.*

private const val CURRENT_LANGUAGE = "current_language"
private const val CURRENT_LANGUAGE_COUNTRY = "current_language_country"

class SharedPrefsCurrentLanguageRepository(private val preferences: SharedPreferences) :
    CurrentLanguageRepository {

    override fun save(language: String, country: String?) {
        with(preferences) {
            edit().putString(CURRENT_LANGUAGE, language).apply()
            edit().putString(CURRENT_LANGUAGE_COUNTRY, country).apply()
        }
    }

    override fun getLanguage(): String? {
        return preferences.getString(CURRENT_LANGUAGE, Locale.getDefault().language)
    }

    override fun getCountry(): String? {
        return preferences.getString(CURRENT_LANGUAGE_COUNTRY, Locale.getDefault().country)
    }
}
