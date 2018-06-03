package chat.rocket.android.dagger.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import chat.rocket.android.server.*
import chat.rocket.android.util.AppJsonAdapterFactory
import chat.rocket.android.util.TimberLogger
import chat.rocket.common.internal.FallbackSealedClassJsonAdapter
import chat.rocket.common.internal.ISO8601Date
import chat.rocket.common.model.TimestampAdapter
import chat.rocket.common.util.CalendarISO8601Converter
import chat.rocket.common.util.Logger
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.internal.AttachmentAdapterFactory
import chat.rocket.core.internal.ReactionsAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
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
    fun provideCurrentServerRepository(prefs: SharedPreferences): CurrentServerRepository {
        return SharedPrefsCurrentServerRepository(prefs)
    }

    @Provides
    @Singleton
    fun provideTokenRepository(prefs: SharedPreferences, moshi: Moshi): TokenRepository {
        return SharedPreferencesTokenRepository(prefs, moshi)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Application) =
        context.getSharedPreferences("rocket.chat", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePlatformLogger(): PlatformLogger {
        return TimberLogger
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

}