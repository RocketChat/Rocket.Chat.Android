package chat.rocket.android.main.di

import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.main.presentation.MainView
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.Provides

@Module
class MainModule {
//    @Provides
//    fun provideLifecycleOwner(activity: MainActivity): LifecycleOwner {
//        return activity
//    }

    @Provides
    @PerActivity
    fun createMainView(activity: MainActivity): MainView {
        return activity
    }

//    @Provides
//    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
//        return CancelStrategy(owner, jobs)
//    }
}