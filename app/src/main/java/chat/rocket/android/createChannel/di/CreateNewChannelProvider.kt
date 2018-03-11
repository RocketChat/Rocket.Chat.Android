package chat.rocket.android.createChannel.di

import chat.rocket.android.createChannel.ui.CreateNewChannelActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CreateNewChannelProvider {
    @ContributesAndroidInjector(modules = [CreateNewChannelModule::class])
    abstract fun provideNewChannelActivity(): CreateNewChannelActivity
}