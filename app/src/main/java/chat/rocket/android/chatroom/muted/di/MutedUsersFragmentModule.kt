package chat.rocket.android.chatroom.muted.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.muted.presentation.MutedUsersView
import chat.rocket.android.chatroom.muted.ui.MutedUsersFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class MutedUsersFragmentModule {
    @Provides
    fun mutedUsersView(frag: MutedUsersFragment): MutedUsersView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: MutedUsersFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}
