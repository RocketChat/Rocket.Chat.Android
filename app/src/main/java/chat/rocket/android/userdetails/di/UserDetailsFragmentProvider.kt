package chat.rocket.android.userdetails.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.userdetails.ui.UserDetailsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserDetailsFragmentProvider {

    @ContributesAndroidInjector(modules = [UserDetailsFragmentModule::class])
    @PerFragment
    abstract fun provideUserDetailsFragment(): UserDetailsFragment
}
