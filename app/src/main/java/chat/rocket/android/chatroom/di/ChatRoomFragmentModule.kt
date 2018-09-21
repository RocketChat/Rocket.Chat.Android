package chat.rocket.android.chatroom.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import javax.inject.Named

@Module
class ChatRoomFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun chatRoomView(frag: ChatRoomFragment): ChatRoomView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ChatRoomFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }

    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()
}
