package chat.rocket.android.authentication.onboarding.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.onboarding.presentation.OnBoardingView
import chat.rocket.android.authentication.onboarding.ui.OnBoardingFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class OnBoardingFragmentModule {

    @Provides
    @PerFragment
    fun onBoardingView(frag: OnBoardingFragment): OnBoardingView = frag

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: OnBoardingFragment): LifecycleOwner = frag
}