package chat.rocket.android.db

import android.app.Application
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManagerFactory @Inject constructor(private val context: Application) {
    private val cache = HashMap<String, DatabaseManager>()

    fun create(serverUrl: String): DatabaseManager {
        cache[serverUrl]?.let {
            Timber.d("Returning cached database for $serverUrl")
            return it
        }

        Timber.d("Returning FRESH database for $serverUrl")
        val db = DatabaseManager(context, serverUrl)
        cache[serverUrl] = db
        return db
    }
}