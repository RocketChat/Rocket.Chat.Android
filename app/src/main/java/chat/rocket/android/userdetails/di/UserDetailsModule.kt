package chat.rocket.android.userdetails.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.userdetails.presentation.UserDetailsView
import chat.rocket.android.userdetails.ui.UserDetailsActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class UserDetailsModule {

    @Provides
    @PerActivity
    fun provideJob() = Job()

    @Provides
    @PerActivity
    fun provideUserDetailsView(activity: UserDetailsActivity): UserDetailsView = activity

    @Provides
    @PerActivity
    fun provideLifecycleOwner(activity: UserDetailsActivity): LifecycleOwner = activity

    @Provides
    @PerActivity
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}
