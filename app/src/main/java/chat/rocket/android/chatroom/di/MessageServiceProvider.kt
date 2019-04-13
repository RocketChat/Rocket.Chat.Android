package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.service.MessageService
import dagger.Module

@Module
abstract class MessageServiceProvider {

    abstract fun provideMessageService(): MessageService
}