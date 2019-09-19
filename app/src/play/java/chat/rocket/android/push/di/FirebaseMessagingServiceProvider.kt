package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.RocketChatMessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FirebaseMessagingServiceProvider {

    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideRocketChatMessagingService(): RocketChatMessagingService
}