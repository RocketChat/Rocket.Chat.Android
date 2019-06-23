package chat.rocket.android.thememanager.di

import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.thememanager.BaseActivity
import chat.rocket.android.thememanager.ui.ThemesFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class ThemesFragmentModule {

    @ContributesAndroidInjector
    @PerFragment
    abstract fun providesThemesFragment(): ThemesFragment
}