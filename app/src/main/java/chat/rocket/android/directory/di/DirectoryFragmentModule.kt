package chat.rocket.android.directory.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.directory.presentation.DirectoryView
import chat.rocket.android.directory.ui.DirectoryFragment
import dagger.Module
import dagger.Provides

@Module
class DirectoryFragmentModule {

    @Provides
    @PerFragment
    fun directoryView(frag: DirectoryFragment): DirectoryView = frag
}