package chat.rocket.android.authentication.onboarding.di

import chat.rocket.android.authentication.onboarding.ui.OnBoardingFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnBoardingFragmentProvider {

    @ContributesAndroidInjector(modules = [OnBoardingFragmentModule::class])
    @PerFragment
    abstract fun provideOnBoardingFragment(): OnBoardingFragment
}