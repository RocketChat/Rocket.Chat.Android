package chat.rocket.android.videoconferencing.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.videoconferencing.presenter.VideoConferencingView
import chat.rocket.android.videoconferencing.ui.VideoConferencingActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job

@Module
class VideoConferencingModule {

    @Provides
    @PerActivity
    fun provideVideoConferencingView(activity: VideoConferencingActivity): VideoConferencingView {
        return activity
    }

    @Provides
    @PerActivity
    fun provideJob() = Job()

    @Provides
    @PerActivity
    fun provideLifecycleOwner(activity: VideoConferencingActivity): LifecycleOwner = activity

    @Provides
    @PerActivity
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy =
        CancelStrategy(owner, jobs)
}