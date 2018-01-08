package chat.rocket.android.push.di

import chat.rocket.android.BuildConfig
import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesRepository
import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenRepository
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@Singleton
class PushModule(val context: FirebaseTokenService) {

    @Provides
    fun provideAuthTokenRepository(): TokenRepository = AuthTokenRepository()

    @Provides
    fun provideLocalRepository(): LocalRepository = SharedPreferencesRepository(context)

    @Provides
    @Singleton
    fun providePlatformLogger(): PlatformLogger {
        return TimberLogger
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        }

        return interceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(logger)
        }.build()
    }

    @Provides
    fun provideRocketChatClient(okHttpClient: OkHttpClient, repository: TokenRepository, logger: PlatformLogger): RocketChatClient {
        return RocketChatClient.create {
            httpClient = okHttpClient
            restUrl = "https://unstable.rocket.chat"
            tokenRepository = repository
            platformLogger = logger
        }
    }
}