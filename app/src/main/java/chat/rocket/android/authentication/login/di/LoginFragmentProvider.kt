package chat.rocket.android.authentication.login.di

import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class LoginFragmentProvider {

    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    @PerFragment
    abstract fun provideLoginFragment(): LoginFragment
}