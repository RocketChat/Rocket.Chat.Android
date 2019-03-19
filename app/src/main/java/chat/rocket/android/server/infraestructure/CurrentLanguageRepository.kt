package chat.rocket.android.server.infraestructure

interface CurrentLanguageRepository {
    fun save(language: String)
    fun get(): String?
}