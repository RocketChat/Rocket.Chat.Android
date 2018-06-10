package chat.rocket.android.server

import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RocketChatClientFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val repository: TokenRepository,
    private val logger: PlatformLogger
) {
    fun create(url: String): RocketChatClient {
        val client = RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = url
            tokenRepository = repository
            platformLogger = logger
        }

        Timber.d("Returning NEW client for: $url")
        return client
    }
}