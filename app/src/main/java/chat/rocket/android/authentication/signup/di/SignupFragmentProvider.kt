package chat.rocket.android.authentication.signup.di

import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SignupFragmentProvider {

    @ContributesAndroidInjector(modules = [SignupFragmentModule::class])
    @PerFragment
    abstract fun provideSignupFragment(): SignupFragment
}