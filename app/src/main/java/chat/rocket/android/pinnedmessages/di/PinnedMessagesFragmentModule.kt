package chat.rocket.android.pinnedmessages.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.pinnedmessages.presentation.PinnedMessagesView
import chat.rocket.android.pinnedmessages.ui.PinnedMessagesFragment
import dagger.Module
import dagger.Provides

@Module
class PinnedMessagesFragmentModule {

    @Provides
    @PerFragment
    fun providePinnedMessagesView(frag: PinnedMessagesFragment): PinnedMessagesView {
        return frag
    }
}