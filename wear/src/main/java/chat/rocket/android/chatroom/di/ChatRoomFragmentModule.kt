package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class ChatRoomFragmentModule {
    @Provides
    @PerFragment
    fun chatRoomView(frag: ChatRoomFragment): ChatRoomView {
        return frag
    }
}