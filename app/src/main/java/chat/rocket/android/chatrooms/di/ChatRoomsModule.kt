package chat.rocket.android.chatrooms.di

import android.content.Context
import chat.rocket.android.chatrooms.presentation.ChatRoomsNavigator
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides

@Module
class ChatRoomsModule {

    @Provides
    @PerActivity
    fun provideChatRoomsNavigator(activity: MainActivity, context: Context) = ChatRoomsNavigator(activity, context)
}