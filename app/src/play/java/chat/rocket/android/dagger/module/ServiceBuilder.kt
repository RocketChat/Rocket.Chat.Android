package chat.rocket.android.dagger.module

import chat.rocket.android.chatroom.di.MessageServiceProvider
import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.push.RocketChatMessagingService
import chat.rocket.android.push.di.FirebaseMessagingServiceProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [FirebaseMessagingServiceProvider::class])
    abstract fun bindRocketChatMessagingService(): RocketChatMessagingService

    @ContributesAndroidInjector(modules = [MessageServiceProvider::class])
    abstract fun bindMessageService(): MessageService
}