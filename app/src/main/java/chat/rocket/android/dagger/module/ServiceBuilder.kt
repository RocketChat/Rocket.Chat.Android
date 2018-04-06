package chat.rocket.android.dagger.module

import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.android.push.GcmListenerService
import chat.rocket.android.push.di.FirebaseTokenServiceProvider
import chat.rocket.android.push.di.GcmListenerServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [FirebaseTokenServiceProvider::class])
    abstract fun bindFirebaseTokenService(): FirebaseTokenService

    @ContributesAndroidInjector(modules = [GcmListenerServiceProvider::class])
    abstract fun bindGcmListenerService(): GcmListenerService
}