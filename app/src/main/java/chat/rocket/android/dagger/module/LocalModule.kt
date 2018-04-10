package chat.rocket.android.dagger.module

import android.content.Context
import android.content.SharedPreferences
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPrefsLocalRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocalModule {

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("rocket.chat", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(prefs: SharedPreferences): LocalRepository {
        return SharedPrefsLocalRepository(prefs)
    }
}