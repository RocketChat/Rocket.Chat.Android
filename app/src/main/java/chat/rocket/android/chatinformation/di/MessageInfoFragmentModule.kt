package chat.rocket.android.chatinformation.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.chatinformation.presentation.MessageInfoView
import chat.rocket.android.chatinformation.ui.MessageInfoFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Job

@Module
class MessageInfoFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun messageInfoView(frag: MessageInfoFragment): MessageInfoView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: MessageInfoFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}
