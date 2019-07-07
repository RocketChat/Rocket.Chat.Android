package chat.rocket.android.db

import android.app.Application
import chat.rocket.android.server.domain.TokenRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManagerFactory @Inject constructor(
    private val context: Application,
    private val tokenRepository: TokenRepository
) {
    private val cache = HashMap<String, DatabaseManager>()

    fun create(serverUrl: String): DatabaseManager? {
        cache[serverUrl]?.let {
            Timber.d("Returning cached database for $serverUrl")
            return it
        }

        tokenRepository.get(serverUrl)?.let { token ->
            DatabaseManager(context, serverUrl, token).apply {
                cache[serverUrl] = this
                Timber.d("Returning fresh database for $serverUrl")
                return this
            }
        }

        return null
    }
}