package chat.rocket.android.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Credentials
import java.io.IOException
import chat.rocket.android.server.domain.model.BasicAuth
import chat.rocket.android.server.domain.GetBasicAuthInteractor
import chat.rocket.android.server.domain.SaveBasicAuthInteractor

import javax.inject.Inject

/**
 * An OkHttp interceptor which adds Authorization header based on URI userInfo
 * part. Can be applied as an
 * [application interceptor][OkHttpClient.interceptors]
 * or as a [ ][OkHttpClient.networkInterceptors].
 */
class BasicAuthenticatorInterceptor @Inject constructor (
    private val getBasicAuthInteractor: GetBasicAuthInteractor,
    private val saveBasicAuthInteractor: SaveBasicAuthInteractor
): Interceptor {
    private val credentials = HashMap<String, String>()

    init {
        val basicAuths = getBasicAuthInteractor.getAll()
        for (basicAuth in basicAuths){
            credentials[basicAuth.host] = basicAuth.credentials
        }
    }

    private fun saveCredentials(host: String, basicCredentials: String) {
        saveBasicAuthInteractor.save(
            BasicAuth(
                host,
                basicCredentials
            )
        )
        credentials[host] = basicCredentials
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url()
        val host = url.host()
        val username = url.username()

        if (!username.isNullOrEmpty()) {
            saveCredentials(host, Credentials.basic(username, url.password()))
            request = request.newBuilder().url(
                url.newBuilder().username("").password("").build()
            ).build()
        }

        credentials[host]?.let {
            request = request.newBuilder().header("Authorization", it).build()
        }

        return chain.proceed(request)
    }
}
