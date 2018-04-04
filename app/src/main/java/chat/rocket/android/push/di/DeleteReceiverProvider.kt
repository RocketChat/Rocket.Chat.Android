package chat.rocket.android.push.di

import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.push.DeleteReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeleteReceiverProvider {
    @ContributesAndroidInjector(modules = [AppModule::class])
    abstract fun provideDeleteReceiver(): DeleteReceiver
}