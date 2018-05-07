package chat.rocket.android.authentication.infraestructure

import android.content.SharedPreferences
import androidx.core.content.edit
import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.common.model.Token
import com.squareup.moshi.Moshi
import timber.log.Timber


class SharedPreferencesTokenRepository(private val prefs: SharedPreferences, moshi: Moshi) : TokenRepository {
    private var servers = prefs.getStringSet(KEY_SERVERS, emptySet()).toMutableSet()
    private var currentUrl: String? = null
    private var currentToken: Token? = null
    private val adapter = moshi.adapter<TokenModel>(TokenModel::class.java)

    override fun get(url: String): Token? {
        if (currentToken != null && url == currentUrl) {
            return currentToken
        }

        try {
            prefs.getString(tokenKey(url), null)?.let { tokenStr ->
                val model = adapter.fromJson(tokenStr)
                model?.let {
                    val token = Token(model.userId, model.authToken)
                    currentToken = token
                    currentUrl = url
                }
            }
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing token for ${tokenKey(url)}")
            ex.printStackTrace()
        }

        return currentToken
    }

    override fun save(url: String, token: Token) {
        try {
            val model = TokenModel(token.userId, token.authToken)
            val str = adapter.toJson(model)

            servers.add(url)

            prefs.edit {
                putString(tokenKey(url), str)
                putStringSet(KEY_SERVERS, servers)
            }

            currentToken = token
            currentUrl = url
        } catch (ex: Exception) {
            Timber.d(ex, "Error saving token for ${tokenKey(url)}")
            ex.printStackTrace()
        }
    }

    override fun remove(url: String) {
        servers.remove(url)
        prefs.edit {
            remove(url)
            putStringSet(KEY_SERVERS, servers)
        }
    }

    override fun clear() {
        servers.forEach { server ->
            prefs.edit { remove(server) }
        }
        servers.clear()
        prefs.edit {
            remove(KEY_SERVERS)
        }
    }

    private fun tokenKey(url: String) = "$KEY_TOKEN$url"
}

private const val KEY_TOKEN = "KEY_TOKEN_"
private const val KEY_SERVERS = "KEY_SERVERS"