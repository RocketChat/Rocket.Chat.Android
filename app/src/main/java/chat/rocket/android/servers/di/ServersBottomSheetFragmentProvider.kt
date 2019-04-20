package chat.rocket.android.servers.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.servers.ui.ServersBottomSheetFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServersBottomSheetFragmentProvider {

    @ContributesAndroidInjector(modules = [ServersBottomSheetFragmentModule::class])
    @PerFragment
    abstract fun provideServersBottomSheetFragment(): ServersBottomSheetFragment
}