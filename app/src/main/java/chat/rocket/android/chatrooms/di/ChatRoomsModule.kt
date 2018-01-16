package chat.rocket.android.chatrooms.di

import android.content.Context
import chat.rocket.android.chatrooms.presentation.ChatRoomsNavigator
import chat.rocket.android.chatrooms.ui.ChatRoomsActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides

@Module
class ChatRoomsModule {

    @Provides
    @PerActivity
    fun provideAuthenticationNavigator(activity: ChatRoomsActivity, context: Context) = ChatRoomsNavigator(activity, context)
}