package chat.rocket.android.chatinformation.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatinformation.presentation.MessageInfoView
import chat.rocket.android.chatinformation.ui.MessageInfoFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class MessageInfoFragmentModule {

    @Provides
    fun messageInfoView(frag: MessageInfoFragment): MessageInfoView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: MessageInfoFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}