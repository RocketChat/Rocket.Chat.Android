package chat.rocket.android.create_channel.di

import chat.rocket.android.create_channel.ui.CreateNewChannelActivity
import dagger.android.ContributesAndroidInjector


abstract class CreateNewChannelProvider {
    @ContributesAndroidInjector(modules = [CreateNewChannelModule::class])
    abstract fun provideNewChannelActivity(): CreateNewChannelActivity
}