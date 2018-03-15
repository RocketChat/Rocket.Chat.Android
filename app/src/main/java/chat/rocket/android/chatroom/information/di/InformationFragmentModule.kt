package chat.rocket.android.chatroom.information.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.information.presentation.InformationView
import chat.rocket.android.chatroom.information.ui.InformationFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class InformationFragmentModule {
    @Provides
    fun informationView(frag: InformationFragment): InformationView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: InformationFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}
