package chat.rocket.android.server.infraestructure

import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.android.server.domain.TokenRepository
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocketChatClientFactory @Inject constructor(private val okHttpClient: OkHttpClient,
                                                  private val repository: TokenRepository,
                                                  private val logger: PlatformLogger) {
    private val cache = HashMap<String, RocketChatClient>()

    fun create(url: String): RocketChatClient {
        cache[url]?.let {
            Timber.d("Returning CACHED client for: $url")
            return it
        }

        val client = RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = url
            tokenRepository = repository
            platformLogger = logger
        }

        Timber.d("Returning NEW client for: $url")
        cache[url] = client
        return client
    }
}