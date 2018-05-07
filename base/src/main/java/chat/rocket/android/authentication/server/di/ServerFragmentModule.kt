package chat.rocket.android.authentication.server.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.server.presentation.ServerView
import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class ServerFragmentModule {

    @Provides
    fun serverView(frag: ServerFragment): ServerView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: ServerFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}