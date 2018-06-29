package chat.rocket.android.main.settings.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.main.settings.ui.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentProvider {

    @ContributesAndroidInjector
    @PerFragment
    abstract fun provideSettingsFragment(): SettingsFragment
}