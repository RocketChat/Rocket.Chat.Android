package chat.rocket.android.createChannel.addMembers.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.createChannel.addMembers.presentation.AddMembersView
import chat.rocket.android.createChannel.addMembers.ui.AddMembersActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class AddMembersModule {
    @Provides
    fun provideLifecycleOwner(activity: AddMembersActivity): LifecycleOwner {
        return activity
    }

    @Provides
    @PerActivity
    fun addMembersView(activity: AddMembersActivity): AddMembersView {
        return activity
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}