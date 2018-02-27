package chat.rocket.android.widget.share.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.presentation.ChatRoomView
import chat.rocket.android.chatroom.ui.ChatRoomFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.widget.share.presentation.ShareView
import chat.rocket.android.widget.share.ui.ShareBottomSheetDialog
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class ShareFragmentModule {

    @Provides
    fun provideShareView(frag: ShareBottomSheetDialog): ShareView {
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