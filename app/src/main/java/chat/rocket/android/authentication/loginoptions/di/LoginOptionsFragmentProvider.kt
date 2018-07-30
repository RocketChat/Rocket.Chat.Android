package chat.rocket.android.authentication.loginoptions.di

import chat.rocket.android.authentication.loginoptions.ui.LoginOptionsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class LoginOptionsFragmentProvider {

    @ContributesAndroidInjector(modules = [LoginOptionsFragmentModule::class])
    @PerFragment
    abstract fun providesLoginOptionFragment(): LoginOptionsFragment
}