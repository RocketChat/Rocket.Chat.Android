package chat.rocket.android.createChannel.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.createChannel.ui.CreateNewChannelActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class CreateNewChannelModule {
    @Provides
    fun provideLifecycleOwner(activity: CreateNewChannelActivity): LifecycleOwner {
        return activity
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }




}