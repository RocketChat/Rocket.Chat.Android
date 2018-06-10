package chat.rocket.android.starter.di

import chat.rocket.android.starter.ui.StarterActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class StarterActivityProvider {

    @ContributesAndroidInjector(modules = [StarterActivityModule::class])
    abstract fun provideMainActivity(): StarterActivity
}