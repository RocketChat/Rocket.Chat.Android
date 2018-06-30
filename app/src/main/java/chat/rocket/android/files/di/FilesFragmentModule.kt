package chat.rocket.android.files.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.files.presentation.FilesView
import chat.rocket.android.files.ui.FilesFragment
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import javax.inject.Named

@Module
class FilesFragmentModule {

    @Provides
    @PerFragment
    fun provideFilesView(frag: FilesFragment): FilesView {
        return frag
    }

    @Provides
    @PerFragment
    @Named("currentServer")
    fun provideCurrentServer(currentServerInteractor: GetCurrentServerInteractor): String {
        return currentServerInteractor.get()!!
    }

    @Provides
    @PerFragment
    fun provideDatabaseManager(
        factory: DatabaseManagerFactory,
        @Named("currentServer") currentServer: String
    ): DatabaseManager {
        return factory.create(currentServer)
    }

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: FilesFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}