package chat.rocket.android.server.infrastructure

interface CurrentLanguageRepository {

    fun save(language: String, country: String? = null)
    fun getLanguage(): String?
    fun getCountry(): String?
}