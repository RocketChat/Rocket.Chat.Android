package chat.rocket.android.server

interface CurrentServerRepository {
    fun save(url: String)
    fun get(): String?
    fun clear()
}