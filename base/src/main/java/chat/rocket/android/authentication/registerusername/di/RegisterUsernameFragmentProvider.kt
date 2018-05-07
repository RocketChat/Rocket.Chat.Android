package chat.rocket.android.authentication.registerusername.di

import chat.rocket.android.authentication.registerusername.ui.RegisterUsernameFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RegisterUsernameFragmentProvider {

    @ContributesAndroidInjector(modules = [RegisterUsernameFragmentModule::class])
    abstract fun provideRegisterUsernameFragment(): RegisterUsernameFragment
}