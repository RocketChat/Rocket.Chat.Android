package chat.rocket.android.server

interface TokenRepository : chat.rocket.core.TokenRepository {
    fun remove(url: String)
    fun clear()
}