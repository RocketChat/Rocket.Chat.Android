package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.service.MessageService
import chat.rocket.android.dagger.module.AppModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class MessageServiceProvider {

    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideMessageService(): MessageService
}