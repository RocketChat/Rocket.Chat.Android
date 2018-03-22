package chat.rocket.android.weblinks.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.weblinks.presentation.WebLinksView
import chat.rocket.android.weblinks.ui.WebLinksFragment
import dagger.Module
import dagger.Provides

@Module
@PerFragment
class WebLinksFragmentModule {

    @Provides
    fun webLinksView(frag: WebLinksFragment): WebLinksView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: WebLinksFragment): LifecycleOwner {
        return frag
    }
}