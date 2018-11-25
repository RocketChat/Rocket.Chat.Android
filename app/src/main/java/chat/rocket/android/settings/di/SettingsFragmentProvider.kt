package chat.rocket.android.settings.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.settings.ui.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentProvider {

    @ContributesAndroidInjector(modules = [SettingsFragmentModule::class])
    @PerFragment
    abstract fun provideSettingsFragment(): SettingsFragment
}
