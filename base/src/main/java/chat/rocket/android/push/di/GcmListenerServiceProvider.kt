package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.GcmListenerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class GcmListenerServiceProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideGcmListenerService(): GcmListenerService
}