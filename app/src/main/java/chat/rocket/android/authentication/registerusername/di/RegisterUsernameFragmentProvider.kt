package chat.rocket.android.authentication.registerusername.di

import chat.rocket.android.authentication.registerusername.ui.RegisterUsernameFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RegisterUsernameFragmentProvider {

    @ContributesAndroidInjector(modules = [RegisterUsernameFragmentModule::class])
    @PerFragment
    abstract fun provideRegisterUsernameFragment(): RegisterUsernameFragment
}