package chat.rocket.android.dagger.module

import chat.rocket.android.push.FirebaseMessagingService
import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.android.push.di.FirebaseMessagingServiceProvider
import chat.rocket.android.push.di.FirebaseTokenServiceProvider
import chat.rocket.android.services.DataLayerListenerService
import chat.rocket.android.services.DataLayerListenerServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {
    @ContributesAndroidInjector(modules = [DataLayerListenerServiceProvider::class])
    abstract fun bindDataLayerListenerService(): DataLayerListenerService

    @ContributesAndroidInjector(modules = [FirebaseTokenServiceProvider::class])
    abstract fun bindFirebaseTokenService(): FirebaseTokenService

    @ContributesAndroidInjector(modules = [FirebaseMessagingServiceProvider::class])
    abstract fun bindFirebaseMessagingService(): FirebaseMessagingService
}