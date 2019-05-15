package chat.rocket.android.directory.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.directory.ui.DirectoryFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DirectoryFragmentProvider {

    @ContributesAndroidInjector(modules = [DirectoryFragmentModule::class])
    @PerFragment
    abstract fun provideDirectoryFragment(): DirectoryFragment

}