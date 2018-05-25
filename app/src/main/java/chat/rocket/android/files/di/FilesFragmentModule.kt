package chat.rocket.android.files.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.files.presentation.FilesView
import chat.rocket.android.files.ui.FilesFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class FilesFragmentModule {

    @Provides
    fun provideLifecycleOwner(frag: FilesFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }

    @Provides
    fun provideFilesView(frag: FilesFragment): FilesView {
        return frag
    }
}