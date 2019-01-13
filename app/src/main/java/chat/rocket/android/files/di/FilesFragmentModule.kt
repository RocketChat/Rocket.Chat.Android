package chat.rocket.android.files.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.files.presentation.FilesView
import chat.rocket.android.files.ui.FilesFragment
import dagger.Module
import dagger.Provides

@Module
class FilesFragmentModule {

    @Provides
    @PerFragment
    fun provideFilesView(frag: FilesFragment): FilesView {
        return frag
    }
}