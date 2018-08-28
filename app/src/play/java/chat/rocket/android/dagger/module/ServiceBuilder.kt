package chat.rocket.android.dagger.module

import chat.rocket.android.chatroom.di.MessageServiceProvider
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.push.FirebaseMessagingService
import chat.rocket.android.push.FirebaseTokenService
import chat.rocket.android.push.di.FirebaseMessagingServiceProvider
import chat.rocket.android.push.di.FirebaseTokenServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [FirebaseTokenServiceProvider::class])
    abstract fun bindFirebaseTokenService(): FirebaseTokenService

    @ContributesAndroidInjector(modules = [FirebaseMessagingServiceProvider::class])
    abstract fun bindGcmListenerService(): FirebaseMessagingService

    @ContributesAndroidInjector(modules = [MessageServiceProvider::class])
    abstract fun bindMessageService(): MessageService
}