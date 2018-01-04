package chat.rocket.android.push.di

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesRepository
import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenRepository
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@Singleton
class PushModule(val context: FirebaseTokenService) {

    @Provides
    fun provideAuthTokenRepository(): TokenRepository = AuthTokenRepository()

    @Provides
    fun provideLocalRepository(): LocalRepository = SharedPreferencesRepository(context)

//    @Provides
//    fun provideCurrentServer() = "https://open.rocket.chat"

    @Provides
    fun provideRocketChatClient(okHttpClient: OkHttpClient, repository: TokenRepository, logger: PlatformLogger): RocketChatClient {
        return RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = HttpUrl.parse("https://unstable.rocket.chat")!!
            websocketUrl = "https://unstable.rocket.chat"
            tokenRepository = repository
            platformLogger = logger
        }
    }
}