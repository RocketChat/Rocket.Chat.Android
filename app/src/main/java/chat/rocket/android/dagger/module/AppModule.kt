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
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesLocalRepository
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.push.PushManager
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.*
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
import chat.rocket.core.internal.ReactionsAdapter
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.listener.RequestLoggingListener
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.spans.SpannableTheme
import timber.log.Timber
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
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideImagePipelineConfig(context: Context, okHttpClient: OkHttpClient): ImagePipelineConfig {
        val listeners = setOf(RequestLoggingListener())

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
    @Singleton
    fun provideSharedPreferences(context: Application) =
        context.getSharedPreferences("rocket.chat", Context.MODE_PRIVATE)


    @Provides
    @ForMessages
    fun provideMessagesSharedPreferences(context: Application) =
        context.getSharedPreferences("messages", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideLocalRepository(prefs: SharedPreferences, moshi: Moshi): LocalRepository {
        return SharedPreferencesLocalRepository(prefs, moshi)
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
    fun providePermissionsRepository(localRepository: LocalRepository, moshi: Moshi): PermissionsRepository {
        return SharedPreferencesPermissionsRepository(localRepository, moshi)
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
    fun provideActiveUsersRepository(): ActiveUsersRepository {
        return MemoryActiveUsersRepository()
    }

    @Provides
    @Singleton
    fun provideMoshi(
        logger: PlatformLogger,
        currentServerInteractor: GetCurrentServerInteractor
    ): Moshi {
        val url = currentServerInteractor.get() ?: ""
        return Moshi.Builder()
            .add(FallbackSealedClassJsonAdapter.ADAPTER_FACTORY)
            .add(AppJsonAdapterFactory.INSTANCE)
            .add(AttachmentAdapterFactory(Logger(logger, url)))
            .add(
                java.lang.Long::class.java,
                ISO8601Date::class.java,
                TimestampAdapter(CalendarISO8601Converter())
            )
            .add(
                Long::class.java,
                ISO8601Date::class.java,
                TimestampAdapter(CalendarISO8601Converter())
            )
            .add(ReactionsAdapter())
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
    fun provideConfiguration(context: Application): SpannableConfiguration {
        val res = context.resources
        return SpannableConfiguration.builder(context)
            .theme(SpannableTheme.builder()
                .linkColor(res.getColor(R.color.colorAccent))
                .build())
            .build()
    }

    @Provides
    fun provideMessageParser(context: Application, configuration: SpannableConfiguration, serverInteractor: GetCurrentServerInteractor, settingsInteractor: GetSettingsInteractor): MessageParser {
        val url = serverInteractor.get()!!
        return MessageParser(context, configuration, settingsInteractor.get(url))
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