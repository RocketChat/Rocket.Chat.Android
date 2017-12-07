package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.ui.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class LoginFragmentProvider {

    @ContributesAndroidInjector(modules = arrayOf(LoginFragmentModule::class))
    abstract fun provideLoginFragment(): LoginFragment
}
