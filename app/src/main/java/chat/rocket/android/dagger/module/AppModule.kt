package chat.rocket.android.dagger.module

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatDatabase
import chat.rocket.android.app.utils.CustomImageFormatConfigurator
import chat.rocket.android.authentication.infraestructure.MemoryTokenRepository
import chat.rocket.android.authentication.infraestructure.SharedPreferencesMultiServerTokenRepository
import chat.rocket.android.dagger.qualifier.ForFresco
import chat.rocket.android.helper.FrescoAuthInterceptor
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPrefsLocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.*
import chat.rocket.android.util.AppJsonAdapterFactory
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenRepository
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.listener.RequestListener
import com.facebook.imagepipeline.listener.RequestLoggingListener
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.il.AsyncDrawableLoader
import ru.noties.markwon.spans.SpannableTheme
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideRocketChatClient(okHttpClient: OkHttpClient, repository: TokenRepository, logger: PlatformLogger): RocketChatClient {
        return RocketChatClient.create {
            httpClient = okHttpClient
            tokenRepository = repository
            platformLogger = logger

            // TODO remove
            restUrl = "https://open.rocket.chat"
        }
    }

    @Provides
    @Singleton
    fun provideRocketChatDatabase(context: Application): RocketChatDatabase {
        return Room.databaseBuilder(context, RocketChatDatabase::class.java, "rocketchat-db").build()
    }

    @Provides
    fun provideJob(): Job {
        return Job()
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

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            Timber.d(message)
        })
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            // TODO - change to HEADERS on production...
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        return interceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            addInterceptor(logger)
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
        }.build()
    }

    @Provides
    @ForFresco
    @Singleton
    fun provideFrescoAuthIntercepter(tokenRepository: TokenRepository): Interceptor {
        return FrescoAuthInterceptor(tokenRepository)
    }

    @Provides
    @ForFresco
    @Singleton
    fun provideFrescoOkHttpClient(okHttpClient: OkHttpClient, @ForFresco authInterceptor: Interceptor): OkHttpClient {
        return okHttpClient.newBuilder().apply {
            //addInterceptor(authInterceptor)
        }.build()
    }

    @Provides
    @Singleton
    fun provideImagePipelineConfig(context: Context, @ForFresco okHttpClient: OkHttpClient): ImagePipelineConfig {
        val listeners = HashSet<RequestListener>()
        listeners.add(RequestLoggingListener())

        return OkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient)
                .setImageDecoderConfig(CustomImageFormatConfigurator.createImageDecoderConfig())
                .setRequestListeners(listeners)
                .setDownsampleEnabled(true)
                //.experiment().setBitmapPrepareToDraw(true).experiment()
                .experiment().setPartialImageCachingEnabled(true).build()
    }

    @Provides
    @Singleton
    fun provideDraweeConfig(): DraweeConfig {
        val draweeConfigBuilder = DraweeConfig.newBuilder()

        CustomImageFormatConfigurator.addCustomDrawableFactories(draweeConfigBuilder)

        return draweeConfigBuilder.build()
    }

    @Provides
    @Singleton
    fun provideTokenRepository(): TokenRepository {
        return MemoryTokenRepository()
    }

    @Provides
    @Singleton
    fun providePlatformLogger(): PlatformLogger {
        return TimberLogger
    }

    @Provides
    fun provideSharedPreferences(context: Application): SharedPreferences {
        return context.getSharedPreferences("rocket.chat", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(prefs: SharedPreferences): LocalRepository {
        return SharedPrefsLocalRepository(prefs)
    }

    @Provides
    @Singleton
    fun provideCurrentServerRepository(prefs: SharedPreferences): CurrentServerRepository {
        return SharedPrefsCurrentServerRepository(prefs)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(localRepository: LocalRepository): SettingsRepository {
        return SharedPreferencesSettingsRepository(localRepository)
    }

    @Provides
    @Singleton
    fun provideChatRoomsRepository(): ChatRoomsRepository {
        return MemoryChatRoomsRepository()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(AppJsonAdapterFactory.INSTANCE).build()
    }

    @Provides
    @Singleton
    fun provideMultiServerTokenRepository(repository: LocalRepository, moshi: Moshi): MultiServerTokenRepository {
        return SharedPreferencesMultiServerTokenRepository(repository, moshi)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(): MessagesRepository {
        return MemoryMessagesRepository()
    }

    @Provides
    @Singleton
    fun provideConfiguration(context: Application, client: OkHttpClient): SpannableConfiguration {
        val res = context.resources
        return SpannableConfiguration.builder(context)
                .asyncDrawableLoader(AsyncDrawableLoader.builder()
                        .client(client)
                        .executorService(Executors.newCachedThreadPool())
                        .resources(res)
                        .build())
                .theme(SpannableTheme.builder()
                        .linkColor(res.getColor(R.color.colorAccent))
                        .build())
                .build()
    }

    @Provides
    @Singleton
    fun provideMessageParser(context: Application, configuration: SpannableConfiguration): MessageParser {
        return MessageParser(context, configuration)
    }

    @Provides
    @Singleton
    fun providePermissionInteractor(settingsRepository: SettingsRepository, serverRepository: CurrentServerRepository): GetPermissionsInteractor {
        return GetPermissionsInteractor(settingsRepository, serverRepository)
    }
}