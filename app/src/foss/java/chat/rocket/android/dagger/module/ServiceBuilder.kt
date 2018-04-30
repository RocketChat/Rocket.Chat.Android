package chat.rocket.android.dagger.module

import chat.rocket.android.chatroom.di.MessageServiceProvider
import chat.rocket.android.chatroom.service.MessageService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServiceBuilder {
    @ContributesAndroidInjector(modules = [MessageServiceProvider::class])
    abstract fun bindMessageService(): MessageService
}
