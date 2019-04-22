package chat.rocket.android.main.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.main.presentation.MainView
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job

@Module
class MainModule {

    @Provides
    @PerActivity
    fun provideJob() = Job()

    @Provides
    @PerActivity
    fun provideMainNavigator(activity: MainActivity) = MainNavigator(activity)

    @Provides
    fun provideMainView(activity: MainActivity): MainView = activity

    @Provides
    fun provideLifecycleOwner(activity: MainActivity): LifecycleOwner = activity

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy =
        CancelStrategy(owner, jobs)
}