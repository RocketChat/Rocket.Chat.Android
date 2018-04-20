package chat.rocket.android.chatroom.edit.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.chatroom.edit.presentation.EditInfoView
import chat.rocket.android.chatroom.edit.ui.EditInfoFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class EditInfoFragmentModule {
    @Provides
    fun editInfoView(frag: EditInfoFragment): EditInfoView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: EditInfoFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}