package chat.rocket.android.chatroom.di

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides

@Module
class ChatRoomModule {
    @Provides
    @PerActivity
    fun provideChatRoomNavigator(activity: ChatRoomActivity) = ChatRoomNavigator(activity)
}