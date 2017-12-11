package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.ui.SignupFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class SignupFragmentProvider {

    @ContributesAndroidInjector(modules = arrayOf(SignupFragmentModule::class))
    abstract fun provideSignupFragment(): SignupFragment
}
