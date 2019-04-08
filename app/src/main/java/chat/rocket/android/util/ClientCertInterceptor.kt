package chat.rocket.android.util

import chat.rocket.android.helper.ClientCertHelper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

/**
 * An OkHttp interceptor which waits for clientCert to be done before overriding the existing
 * okHttpClient, if enabled.
 * [application interceptor][OkHttpClient.interceptors]
 * or as a [ ][OkHttpClient.networkInterceptors].
 */
class ClientCertInterceptor(
        private val clientCertHelper: ClientCertHelper
) : Interceptor {
    @Volatile
    internal var clientOverride: OkHttpClient? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (clientCertHelper.getSetSslSocket()) {
            clientOverride = clientCertHelper.getClient()
        }
        val override = clientOverride
        return if (override != null) {
            override.newCall(chain.request()).execute()
        } else chain.proceed(chain.request())
    }
}
