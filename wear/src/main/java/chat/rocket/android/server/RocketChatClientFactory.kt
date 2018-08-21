package chat.rocket.android.server

import android.os.Build
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
            userAgent = "RC Mobile; Android ${Build.VERSION.RELEASE}; v1.0.0 (1)"
            tokenRepository = repository
            platformLogger = logger
        }

        Timber.d("Returning NEW client for: $url")
        return client
    }
}