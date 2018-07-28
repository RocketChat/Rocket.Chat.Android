package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.DirectReplyReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DirectReplyReceiverProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideDirectReplyReceiver(): DirectReplyReceiver
}