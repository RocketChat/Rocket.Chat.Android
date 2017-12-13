package chat.rocket.android.authentication.server.di

import chat.rocket.android.authentication.server.presentation.ServerView
import chat.rocket.android.authentication.server.ui.ServerFragment
import dagger.Module
import dagger.Provides

@Module
class ServerFragmentModule {
    @Provides
    fun serverView(frag: ServerFragment): ServerView {
        return frag
    }
}
