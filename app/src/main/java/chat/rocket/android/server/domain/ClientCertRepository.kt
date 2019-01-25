package chat.rocket.android.server.domain

interface ClientCertRepository {
    fun save(alias: String)
    fun get(): String?
    fun clear()
}
