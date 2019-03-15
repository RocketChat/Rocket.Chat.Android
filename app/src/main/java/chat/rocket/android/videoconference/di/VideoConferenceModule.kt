package chat.rocket.android.videoconference.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.videoconference.presenter.JitsiVideoConferenceView
import chat.rocket.android.videoconference.ui.VideoConferenceActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job

@Module
class VideoConferenceModule {

    @Provides
    @PerActivity
    fun provideVideoConferenceView(activity: VideoConferenceActivity): JitsiVideoConferenceView {
        return activity
    }

    @Provides
    @PerActivity
    fun provideJob() = Job()

    @Provides
    @PerActivity
    fun provideLifecycleOwner(activity: VideoConferenceActivity): LifecycleOwner = activity

    @Provides
    @PerActivity
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy =
        CancelStrategy(owner, jobs)
}