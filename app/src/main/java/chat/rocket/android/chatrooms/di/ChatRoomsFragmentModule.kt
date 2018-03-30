package chat.rocket.android.chatrooms.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatrooms.presentation.ChatRoomsView
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
@PerFragment
class ChatRoomsFragmentModule {

    @Provides
    fun chatRoomsView(frag: ChatRoomsFragment): ChatRoomsView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: ChatRoomsFragment): LifecycleOwner {
        return frag
    }
}