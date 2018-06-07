package chat.rocket.android.starter.di

import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.starter.presentation.StarterActivityView
import chat.rocket.android.starter.ui.StarterActivity
import dagger.Module
import dagger.Provides

@Module
class StarterActivityModule {
//    @Provides
//    fun provideLifecycleOwner(activity: StarterActivity): LifecycleOwner {
//        return activity
//    }

    @Provides
    @PerActivity
    fun createStarterActivityView(activity: StarterActivity): StarterActivityView {
        return activity
    }

//    @Provides
//    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
//        return CancelStrategy(owner, jobs)
//    }
}