package chat.rocket.android.dagger.module

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import chat.rocket.android.BuildConfig
import chat.rocket.android.app.RocketChatDatabase
import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.server.infraestructure.ServerDao
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideRocketChatDatabase(context: Application): RocketChatDatabase {
        return Room.databaseBuilder(context, RocketChatDatabase::class.java, "rocketchat-db").build()
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideServerDao(database: RocketChatDatabase): ServerDao {
        return database.serverDao()
    }

    /*@Provides
    @Singleton
    @IntoSet
    fun provideHttpLoggingInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            interceptor.level = HttpLoggingInterceptor.Level.HEADERS
        }

        return interceptor
    }*/

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
    @Singleton
    fun provideAuthTokenRepository(): AuthTokenRepository {
        return AuthTokenRepository()
    }

    @Provides
    @Singleton
    fun providePlatformLogger(): PlatformLogger {
        return TimberLogger
    }
}
