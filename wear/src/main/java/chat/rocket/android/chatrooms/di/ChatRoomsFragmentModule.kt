package chat.rocket.android.chatrooms.di

import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class ChatRoomsFragmentModule {

    @Provides
    @PerFragment
    fun chatRoomsView(frag: ChatRoomsFragment): ChatRoomsView {
        return frag
    }
}