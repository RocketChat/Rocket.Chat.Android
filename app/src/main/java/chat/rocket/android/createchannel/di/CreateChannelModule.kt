package chat.rocket.android.createchannel.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.createchannel.presentation.CreateChannelView
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class CreateChannelModule {

    @Provides
    @PerFragment
    fun createChannelView(fragment: CreateChannelFragment): CreateChannelView {
        return fragment
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(fragment: CreateChannelFragment): LifecycleOwner {
        return fragment
    }
}