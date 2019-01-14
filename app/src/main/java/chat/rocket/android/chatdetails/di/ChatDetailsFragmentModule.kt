package chat.rocket.android.chatdetails.di

import chat.rocket.android.chatdetails.presentation.ChatDetailsView
import chat.rocket.android.chatdetails.ui.ChatDetailsFragment
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import dagger.Module
import dagger.Provides

@Module
class ChatDetailsFragmentModule {

    @Provides
    @PerFragment
    fun chatDetailsView(frag: ChatDetailsFragment): ChatDetailsView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()
}