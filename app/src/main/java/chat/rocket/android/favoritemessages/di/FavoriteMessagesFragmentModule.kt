package chat.rocket.android.chatroom.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.favoritemessages.ui.FavoriteMessagesFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class FavoriteMessagesFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: FavoriteMessagesFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }

    @Provides
    @PerFragment
    fun provideFavoriteMessagesView(frag: FavoriteMessagesFragment): FavoriteMessagesView {
        return frag
    }
}