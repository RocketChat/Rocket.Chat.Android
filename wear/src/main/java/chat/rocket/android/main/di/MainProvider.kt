package chat.rocket.android.wear.main.di

import chat.rocket.android.main.di.MainModule
import chat.rocket.android.main.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainProvider {

    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun provideMainActivity(): MainActivity
}