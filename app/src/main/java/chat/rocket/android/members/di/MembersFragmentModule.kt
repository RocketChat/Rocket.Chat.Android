package chat.rocket.android.members.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.members.presentation.MembersNavigator
import chat.rocket.android.members.presentation.MembersView
import chat.rocket.android.members.ui.MembersFragment
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job
import javax.inject.Named

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
    @Named("currentServer")
    fun provideCurrentServer(currentServerInteractor: GetCurrentServerInteractor): String {
        return currentServerInteractor.get()!!
    }

    @Provides
    @PerFragment
    fun provideDatabaseManager(
        factory: DatabaseManagerFactory,
        @Named("currentServer") currentServer: String
    ): DatabaseManager {
        return factory.create(currentServer)
    }

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