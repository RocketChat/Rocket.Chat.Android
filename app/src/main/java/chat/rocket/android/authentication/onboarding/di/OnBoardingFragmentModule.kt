package chat.rocket.android.authentication.onboarding.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingView
import chat.rocket.android.authentication.onboarding.ui.OnBoardingFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class OnBoardingFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun onBoardingView(frag: OnBoardingFragment): OnBoardingView = frag

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: OnBoardingFragment): LifecycleOwner = frag

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy =
        CancelStrategy(owner, jobs)
}