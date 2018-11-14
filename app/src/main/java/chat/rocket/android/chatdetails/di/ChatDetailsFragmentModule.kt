package chat.rocket.android.chatdetails.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatdetails.presentation.ChatDetailsView
import chat.rocket.android.chatdetails.ui.ChatDetailsFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.DatabaseManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class ChatDetailsFragmentModule {
    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun chatDetailsView(frag: ChatDetailsFragment): ChatDetailsView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideChatRoomDao(manager: DatabaseManager): ChatRoomDao = manager.chatRoomDao()

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: ChatDetailsFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}