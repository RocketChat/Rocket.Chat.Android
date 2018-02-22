package chat.rocket.android.authentication.server.di

import chat.rocket.android.authentication.server.ui.ServerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServerFragmentProvider {

    @ContributesAndroidInjector(modules = [ServerFragmentModule::class])
    abstract fun provideServerFragment(): ServerFragment
}