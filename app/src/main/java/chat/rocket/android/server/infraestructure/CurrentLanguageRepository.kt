package chat.rocket.android.server.infraestructure

interface CurrentLanguageRepository {

    fun save(language: String, country: String? = null)
    fun getLanguage(): String?
    fun getCountry(): String?
}