package chat.rocket.android.members.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.ui.MembersFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class MembersFragmentModule {

    @Provides
    fun membersView(frag: MembersFragment): MembersView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: MembersFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}