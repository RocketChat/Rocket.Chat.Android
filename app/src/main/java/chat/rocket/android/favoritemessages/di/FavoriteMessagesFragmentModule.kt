package chat.rocket.android.chatroom.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.favoritemessages.ui.FavoriteMessagesFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class FavoriteMessagesFragmentModule {

    @Provides
    fun provideLifecycleOwner(frag: FavoriteMessagesFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }

    @Provides
    fun provideFavoriteMessagesView(frag: FavoriteMessagesFragment): FavoriteMessagesView {
        return frag
    }
}