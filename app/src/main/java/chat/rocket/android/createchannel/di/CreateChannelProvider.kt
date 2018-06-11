package chat.rocket.android.createchannel.di

import chat.rocket.android.createchannel.ui.CreateChannelFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CreateChannelProvider {

    @ContributesAndroidInjector(modules = [CreateChannelModule::class])
    abstract fun provideCreateChannelFragment(): CreateChannelFragment
}