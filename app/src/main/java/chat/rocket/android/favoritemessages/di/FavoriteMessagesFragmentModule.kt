package chat.rocket.android.favoritemessages.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.favoritemessages.presentation.FavoriteMessagesView
import chat.rocket.android.favoritemessages.ui.FavoriteMessagesFragment
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import javax.inject.Named

@Module
class FavoriteMessagesFragmentModule {

    @Provides
    @PerFragment
    fun provideFavoriteMessagesView(frag: FavoriteMessagesFragment): FavoriteMessagesView {
        return frag
    }

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
}