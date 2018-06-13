package chat.rocket.android.settings.password.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.settings.password.ui.PasswordFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PasswordFragmentProvider {
    @ContributesAndroidInjector(modules = [PasswordFragmentModule::class])
    @PerFragment
    abstract fun providePasswordFragment(): PasswordFragment
}