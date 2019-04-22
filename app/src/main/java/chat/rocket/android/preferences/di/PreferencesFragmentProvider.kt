package chat.rocket.android.preferences.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.preferences.ui.PreferencesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PreferencesFragmentProvider {

    @ContributesAndroidInjector(modules = [PreferencesFragmentModule::class])
    @PerFragment
    abstract fun providePreferencesFragment(): PreferencesFragment
}