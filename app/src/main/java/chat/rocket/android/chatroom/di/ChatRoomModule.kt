package chat.rocket.android.chatroom.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job

@Module
class ChatRoomModule {

    @Provides
    @PerActivity
    fun provideChatRoomNavigator(activity: ChatRoomActivity) = ChatRoomNavigator(activity)

    @Provides
    @PerActivity
    fun provideJob() = Job()

    @Provides
    @PerActivity
    fun provideLifecycleOwner(activity: ChatRoomActivity): LifecycleOwner = activity

    @Provides
    @PerActivity
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}