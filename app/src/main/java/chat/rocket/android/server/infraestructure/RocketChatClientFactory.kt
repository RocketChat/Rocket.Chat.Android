package chat.rocket.android.server.infraestructure

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import okhttp3.OkHttpClient
import javax.inject.Inject

class RocketChatClientFactory @Inject constructor(private val okHttpClient: OkHttpClient,
                                                  private val repository: AuthTokenRepository,
                                                  private val logger: PlatformLogger) {
    private val cache = HashMap<String, RocketChatClient>()

    fun create(url: String): RocketChatClient {
        cache[url]?.let {
            return it
        }

        val client = RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = url
            tokenRepository = repository
            platformLogger = logger
        }

        cache.put(url, client)
        return client
    }
}