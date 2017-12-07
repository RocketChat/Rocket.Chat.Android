package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.presentation.ServerView
import chat.rocket.android.authentication.ui.ServerFragment
import dagger.Module
import dagger.Provides

@Module
class ServerFragmentModule {
    @Provides
    fun serverView(frag: ServerFragment): ServerView {
        return frag
    }
}
