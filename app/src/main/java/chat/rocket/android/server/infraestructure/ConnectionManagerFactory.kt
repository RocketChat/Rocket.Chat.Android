package chat.rocket.android.server.infraestructure

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManagerFactory @Inject constructor(private val factory: RocketChatClientFactory) {
    private val cache = HashMap<String, ConnectionManager>()

    fun create(url: String): ConnectionManager {
        cache[url]?.let {
            Timber.d("Returning CACHED Manager for: $url")
            return it
        }

        Timber.d("Returning FRESH Manager for: $url")
        val manager = ConnectionManager(factory.create(url))
        cache[url] = manager
        return manager
    }

    fun get(url: String) = cache[url]
}