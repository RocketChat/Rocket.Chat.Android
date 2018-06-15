package chat.rocket.android.createchannel.di

import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CreateChannelProvider {

    @ContributesAndroidInjector(modules = [CreateChannelModule::class])
    @PerFragment
    abstract fun provideCreateChannelFragment(): CreateChannelFragment
}