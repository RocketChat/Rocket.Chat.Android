package chat.rocket.android.helper

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.TokenRepository
import okhttp3.Interceptor
import okhttp3.Response

class FrescoAuthInterceptor(
        private val tokenRepository: TokenRepository,
        private val currentServerInteractor: GetCurrentServerInteractor
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        currentServerInteractor.get()?.let { serverUrl ->
            val token = tokenRepository.get(serverUrl)


            return@let token?.let {
                val url = request.url().newBuilder().apply {
                    addQueryParameter("rc_uid", token.userId)
                    addQueryParameter("rc_token", token.authToken)
                }.build()
                request = request.newBuilder().apply {
                    url(url)
                }.build()
            }
        }
        return chain.proceed(request)
    }
}