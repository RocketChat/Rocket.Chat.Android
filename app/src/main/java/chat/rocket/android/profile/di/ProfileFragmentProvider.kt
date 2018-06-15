package chat.rocket.android.profile.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.profile.ui.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileFragmentProvider {

    @ContributesAndroidInjector(modules = [ProfileFragmentModule::class])
    @PerFragment
    abstract fun provideProfileFragment(): ProfileFragment
}