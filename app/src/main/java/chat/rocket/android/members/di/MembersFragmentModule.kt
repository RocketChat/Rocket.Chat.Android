package chat.rocket.android.members.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.members.presentation.MembersNavigator
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.ui.MembersFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class MembersFragmentModule {

    @Provides
    @PerFragment
    fun membersView(frag: MembersFragment): MembersView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideChatRoomNavigator(activity: ChatRoomActivity) = MembersNavigator(activity)

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: MembersFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}