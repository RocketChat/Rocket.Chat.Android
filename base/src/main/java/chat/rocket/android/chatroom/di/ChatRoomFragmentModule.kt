package chat.rocket.android.chatroom.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class ChatRoomFragmentModule {

    @Provides
    fun chatRoomView(frag: ChatRoomFragment): ChatRoomView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: ChatRoomFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}