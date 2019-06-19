package chat.rocket.android.thememanager.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.thememanager.ui.ThemesFragment
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ThemesFragmentProvider {

//    @ContributesAndroidInjector(modules = [ThemesFragmentModule::class])
//    @PerFragment
//    abstract fun provideThemesViewModelFactory(): ThemesViewModelFactory
}