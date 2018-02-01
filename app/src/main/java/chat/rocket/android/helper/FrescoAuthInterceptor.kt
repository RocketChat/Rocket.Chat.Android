package chat.rocket.android.helper

import chat.rocket.core.TokenRepository
import okhttp3.Interceptor
import okhttp3.Response

class FrescoAuthInterceptor(private val tokenRepository: TokenRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenRepository.get()
        var request = chain.request()

        token?.let {
            val url = request.url().newBuilder().apply {
                addQueryParameter("rc_uid", token.userId)
                addQueryParameter("rc_token", token.authToken)
            }.build()
            request = request.newBuilder().apply {
                url(url)
            }.build()
        }

        return chain.proceed(request)
    }
}