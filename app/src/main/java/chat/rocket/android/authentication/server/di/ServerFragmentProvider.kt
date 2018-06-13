package chat.rocket.android.authentication.server.di

import chat.rocket.android.authentication.server.ui.ServerFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServerFragmentProvider {

    @ContributesAndroidInjector(modules = [ServerFragmentModule::class])
    @PerFragment
    abstract fun provideServerFragment(): ServerFragment
}