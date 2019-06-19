package chat.rocket.android.thememanager.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.thememanager.ui.ThemesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ThemesFragmentProvider {

    @ContributesAndroidInjector
    @PerFragment
    abstract fun providesThemesFragment(): ThemesFragment
}