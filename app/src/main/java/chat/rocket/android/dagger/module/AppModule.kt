package chat.rocket.android.dagger.module

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.arch.persistence.room.Room
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatDatabase
import chat.rocket.android.authentication.infraestructure.SharedPreferencesMultiServerTokenRepository
import chat.rocket.android.authentication.infraestructure.SharedPreferencesTokenRepository
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.dagger.qualifier.ForFresco
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.helper.FrescoAuthInterceptor
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPrefsLocalRepository
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.push.PushManager
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.ChatRoomsRepository
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetPermissionsInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.JobSchedulerInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.RoomRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.UsersRepository
import chat.rocket.android.server.infraestructure.JobSchedulerInteractorImpl
import chat.rocket.android.server.infraestructure.MemoryChatRoomsRepository
import chat.rocket.android.server.infraestructure.MemoryRoomRepository
import chat.rocket.android.server.infraestructure.MemoryUsersRepository
import chat.rocket.android.server.infraestructure.ServerDao
import chat.rocket.android.server.infraestructure.SharedPreferencesAccountsRepository
import chat.rocket.android.server.infraestructure.SharedPreferencesMessagesRepository
import chat.rocket.android.server.infraestructure.SharedPreferencesSettingsRepository
import chat.rocket.android.server.infraestructure.SharedPrefsCurrentServerRepository
import chat.rocket.android.util.AppJsonAdapterFactory
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.internal.FallbackSealedClassJsonAdapter
import chat.rocket.common.internal.ISO8601Date
import chat.rocket.common.model.TimestampAdapter
import chat.rocket.common.util.CalendarISO8601Converter
import chat.rocket.common.util.Logger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.AttachmentAdapterFactory
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
        return Room.databaseBuilder(context.applicationContext, RocketChatDatabase::class.java, "rocketchat-db").build()
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
    fun provideFrescoAuthInterceptor(tokenRepository: TokenRepository, currentServerInteractor: GetCurrentServerInteractor): Interceptor {
        return FrescoAuthInterceptor(tokenRepository, currentServerInteractor)
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
                .setRequestListeners(listeners)
                .setDownsampleEnabled(true)
                //.experiment().setBitmapPrepareToDraw(true).experiment()
                .experiment().setPartialImageCachingEnabled(true).build()
    }

    @Provides
    @Singleton
    fun provideDraweeConfig(): DraweeConfig {
        return DraweeConfig.newBuilder().build()
    }

    @Provides
    @Singleton
    fun provideTokenRepository(prefs: SharedPreferences, moshi: Moshi): TokenRepository {
        return SharedPreferencesTokenRepository(prefs, moshi)
    }

    @Provides
    @Singleton
    fun providePlatformLogger(): PlatformLogger {
        return TimberLogger
    }

    @Provides
    fun provideSharedPreferences(context: Application) =
        context.getSharedPreferences("rocket.chat", Context.MODE_PRIVATE)


    @Provides
    @ForMessages
    fun provideMessagesSharedPreferences(context: Application) =
            context.getSharedPreferences("messages", Context.MODE_PRIVATE)

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
    fun provideRoomRepository(): RoomRepository {
        return MemoryRoomRepository()
    }

    @Provides
    @Singleton
    fun provideChatRoomRepository(): ChatRoomsRepository {
        return MemoryChatRoomsRepository()
    }

    @Provides
    @Singleton
    fun provideMoshi(logger: PlatformLogger,
                     currentServerInteractor: GetCurrentServerInteractor):
            Moshi {
        val url = currentServerInteractor.get() ?: ""
        return Moshi.Builder()
                .add(FallbackSealedClassJsonAdapter.ADAPTER_FACTORY)
                .add(AppJsonAdapterFactory.INSTANCE)
                .add(AttachmentAdapterFactory(Logger(logger, url)))
                .add(java.lang.Long::class.java, ISO8601Date::class.java, TimestampAdapter(CalendarISO8601Converter()))
                .add(Long::class.java, ISO8601Date::class.java, TimestampAdapter(CalendarISO8601Converter()))
                .build()
    }

    @Provides
    @Singleton
    fun provideMultiServerTokenRepository(repository: LocalRepository, moshi: Moshi): MultiServerTokenRepository {
        return SharedPreferencesMultiServerTokenRepository(repository, moshi)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(@ForMessages preferences: SharedPreferences,
                                 moshi: Moshi,
                                 currentServerInteractor: GetCurrentServerInteractor): MessagesRepository {
        return SharedPreferencesMessagesRepository(preferences, moshi, currentServerInteractor)
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UsersRepository {
        return MemoryUsersRepository()
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

    @Provides
    @Singleton
    fun provideAccountsRepository(preferences: SharedPreferences, moshi: Moshi): AccountsRepository =
            SharedPreferencesAccountsRepository(preferences, moshi)

    @Provides
    fun provideNotificationManager(context: Application) =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideGroupedPush() = GroupedPush()

    @Provides
    @Singleton
    fun providePushManager(
            context: Application,
            groupedPushes: GroupedPush,
            manager: NotificationManager,
            moshi: Moshi,
            getAccountInteractor: GetAccountInteractor,
            getSettingsInteractor: GetSettingsInteractor): PushManager {
        return PushManager(groupedPushes, manager, moshi, getAccountInteractor, getSettingsInteractor, context)
    }

    @Provides
    fun provideJobScheduler(context: Application): JobScheduler {
        return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    @Provides
    fun provideSendMessageJob(context: Application): JobInfo {
        return JobInfo.Builder(MessageService.RETRY_SEND_MESSAGE_ID,
                ComponentName(context, MessageService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()
    }

    @Provides
    fun provideJobSchedulerInteractor(jobScheduler: JobScheduler, jobInfo: JobInfo): JobSchedulerInteractor {
        return JobSchedulerInteractorImpl(jobScheduler, jobInfo)
    }
}