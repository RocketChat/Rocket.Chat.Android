package chat.rocket.android.services

import chat.rocket.android.dagger.module.AppModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DataLayerListenerServiceProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideDataLayerListenerService(): DataLayerListenerService
}