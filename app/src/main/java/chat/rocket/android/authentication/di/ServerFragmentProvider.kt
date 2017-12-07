package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.ui.ServerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class ServerFragmentProvider {

    @ContributesAndroidInjector(modules = arrayOf(ServerFragmentModule::class))
    abstract fun provideServerFragment(): ServerFragment
}