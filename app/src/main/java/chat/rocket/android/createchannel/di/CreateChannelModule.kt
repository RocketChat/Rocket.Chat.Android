package chat.rocket.android.createchannel.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.createchannel.presentation.CreateChannelView
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
@PerFragment
class CreateChannelModule {

    @Provides
    fun createChannelView(fragment: CreateChannelFragment): CreateChannelView {
        return fragment
    }

    @Provides
    fun provideLifecycleOwner(fragment: CreateChannelFragment): LifecycleOwner {
        return fragment
    }
}