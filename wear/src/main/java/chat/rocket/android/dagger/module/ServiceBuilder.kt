package chat.rocket.android.dagger.module

import chat.rocket.android.services.DataLayerListenerService
import chat.rocket.android.services.DataLayerListenerServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {
    @ContributesAndroidInjector(modules = [DataLayerListenerServiceProvider::class])
    abstract fun bindDataLayerListenerService(): DataLayerListenerService
}