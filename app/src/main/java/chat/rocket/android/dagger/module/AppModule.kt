package chat.rocket.android.dagger.module

import android.app.Application
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.AnswersAnalytics
import chat.rocket.android.analytics.GoogleAnalyticsForFirebase
import chat.rocket.android.authentication.infraestructure.SharedPreferencesMultiServerTokenRepository
import chat.rocket.android.authentication.infraestructure.SharedPreferencesTokenRepository
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.dagger.qualifier.ForAuthentication
import chat.rocket.android.dagger.qualifier.ForMessages
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesLocalRepository
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.push.PushManager
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.server.domain.AnalyticsTrackingRepository
import chat.rocket.android.server.domain.ChatRoomsRepository
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.JobSchedulerInteractor
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.PermissionsRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.UsersRepository
import chat.rocket.android.server.domain.BasicAuthRepository
import chat.rocket.android.server.domain.GetBasicAuthInteractor
import chat.rocket.android.server.domain.SaveBasicAuthInteractor
import chat.rocket.android.server.infraestructure.SharedPrefsBasicAuthRepository
import chat.rocket.android.server.infraestructure.DatabaseMessageMapper
import chat.rocket.android.server.infraestructure.DatabaseMessagesRepository
import chat.rocket.android.server.infraestructure.JobSchedulerInteractorImpl
import chat.rocket.android.server.infraestructure.MemoryChatRoomsRepository
import chat.rocket.android.server.infraestructure.MemoryUsersRepository
import chat.rocket.android.server.infraestructure.SharedPreferencesAccountsRepository
import chat.rocket.android.server.infraestructure.SharedPreferencesPermissionsRepository
import chat.rocket.android.server.infraestructure.SharedPreferencesSettingsRepository
import chat.rocket.android.server.infraestructure.SharedPrefsAnalyticsTrackingRepository
import chat.rocket.android.server.infraestructure.SharedPrefsConnectingServerRepository
import chat.rocket.android.server.infraestructure.SharedPrefsCurrentServerRepository
import chat.rocket.android.util.AppJsonAdapterFactory
import chat.rocket.android.util.HttpLoggingInterceptor
import chat.rocket.android.util.BasicAuthenticatorInterceptor
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.internal.FallbackSealedClassJsonAdapter
import chat.rocket.common.internal.ISO8601Date
import chat.rocket.common.model.TimestampAdapter
import chat.rocket.common.util.CalendarISO8601Converter
import chat.rocket.common.util.NoOpLogger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.internal.AttachmentAdapterFactory
import chat.rocket.core.internal.ReactionsAdapter
import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.listener.RequestLoggingListener
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.spans.SpannableTheme
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Timber.d(message)
            }
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
    fun provideBasicAuthenticatorInterceptor(
        getBasicAuthInteractor: GetBasicAuthInteractor,
        saveBasicAuthInteractor: SaveBasicAuthInteractor
    ): BasicAuthenticatorInterceptor {
        return BasicAuthenticatorInterceptor(
            getBasicAuthInteractor,
            saveBasicAuthInteractor
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logger: HttpLoggingInterceptor, basicAuthenticator: BasicAuthenticatorInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(basicAuthenticator)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideImagePipelineConfig(
        context: Context,
        okHttpClient: OkHttpClient
    ): ImagePipelineConfig {
        val listeners = setOf(RequestLoggingListener())

        return OkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient)
            .setRequestListeners(listeners)
            .setDownsampleEnabled(true)
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
    fun provideAnalyticsTrackingRepository(prefs: SharedPreferences): AnalyticsTrackingRepository {
        return SharedPrefsAnalyticsTrackingRepository(prefs)
    }

    @Provides
    @ForAuthentication
    fun provideConnectingServerRepository(prefs: SharedPreferences): CurrentServerRepository {
        return SharedPrefsConnectingServerRepository(prefs)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(localRepository: LocalRepository): SettingsRepository {
        return SharedPreferencesSettingsRepository(localRepository)
    }

    @Provides
    @Singleton
    fun providePermissionsRepository(
        localRepository: LocalRepository,
        moshi: Moshi
    ): PermissionsRepository {
        return SharedPreferencesPermissionsRepository(localRepository, moshi)
    }

    @Provides
    @Singleton
    fun provideChatRoomRepository(): ChatRoomsRepository {
        return MemoryChatRoomsRepository()
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
            .add(AttachmentAdapterFactory(NoOpLogger))
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
    fun provideMultiServerTokenRepository(
        repository: LocalRepository,
        moshi: Moshi
    ): MultiServerTokenRepository {
        return SharedPreferencesMultiServerTokenRepository(repository, moshi)
    }

    @Provides
    fun provideMessageRepository(databaseManager: DatabaseManager): MessagesRepository {
        return DatabaseMessagesRepository(databaseManager, DatabaseMessageMapper(databaseManager))
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
            .theme(
                SpannableTheme.builder()
                    .blockMargin(0)
                    .linkColor(res.getColor(R.color.colorAccent))
                    .build()
            )
            .build()
    }

    @Provides
    fun provideMessageParser(
        context: Application,
        configuration: SpannableConfiguration,
        serverInteractor: GetCurrentServerInteractor,
        settingsInteractor: GetSettingsInteractor
    ): MessageParser {
        val url = serverInteractor.get()!!
        return MessageParser(context, configuration, settingsInteractor.get(url))
    }

    @Provides
    @Singleton
    fun provideBasicAuthRepository (
        preferences: SharedPreferences,
        moshi: Moshi
    ): BasicAuthRepository = 
        SharedPrefsBasicAuthRepository(preferences, moshi)

    @Provides
    @Singleton
    fun provideAccountsRepository(
        preferences: SharedPreferences,
        moshi: Moshi
    ): AccountsRepository =
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
        getSettingsInteractor: GetSettingsInteractor
    ): PushManager {
        return PushManager(
            groupedPushes,
            manager,
            moshi,
            getAccountInteractor,
            getSettingsInteractor,
            context
        )
    }

    @Provides
    fun provideJobScheduler(context: Application): JobScheduler {
        return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    @Provides
    fun provideSendMessageJob(context: Application): JobInfo {
        return JobInfo.Builder(
            MessageService.RETRY_SEND_MESSAGE_ID,
            ComponentName(context, MessageService::class.java)
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
    }

    @Provides
    fun provideJobSchedulerInteractor(
        jobScheduler: JobScheduler,
        jobInfo: JobInfo
    ): JobSchedulerInteractor {
        return JobSchedulerInteractorImpl(jobScheduler, jobInfo)
    }

    @Provides
    @Named("currentServer")
    fun provideCurrentServer(currentServerInteractor: GetCurrentServerInteractor): String {
        return currentServerInteractor.get()!!
    }

    @Provides
    fun provideDatabaseManager(
        factory: DatabaseManagerFactory,
        @Named("currentServer") currentServer: String
    ): DatabaseManager {
        return factory.create(currentServer)
    }

    @Provides
    @Singleton
    fun provideAnswersAnalytics(): AnswersAnalytics {
        return AnswersAnalytics()
    }

    @Provides
    @Singleton
    fun provideGoogleAnalyticsForFirebase(context: Application): GoogleAnalyticsForFirebase {
        return GoogleAnalyticsForFirebase(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(
        analyticsTrackingInteractor: AnalyticsTrackingInteractor,
        getCurrentServerInteractor: GetCurrentServerInteractor,
        getAccountsInteractor: GetAccountsInteractor,
        answersAnalytics: AnswersAnalytics,
        googleAnalyticsForFirebase: GoogleAnalyticsForFirebase
    ): AnalyticsManager {
        return AnalyticsManager(
            analyticsTrackingInteractor,
            getCurrentServerInteractor,
            getAccountsInteractor,
            listOf(answersAnalytics, googleAnalyticsForFirebase)
        )
    }
}
