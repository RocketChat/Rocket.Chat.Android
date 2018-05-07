package chat.rocket.android.authentication.login.di

import chat.rocket.android.authentication.login.ui.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class LoginFragmentProvider {

    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun provideLoginFragment(): LoginFragment
}