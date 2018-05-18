package chat.rocket.android.authentication.resetpassword.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.resetpassword.presentation.ResetPasswordView
import chat.rocket.android.authentication.resetpassword.ui.ResetPasswordFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class ResetPasswordFragmentModule {

    @Provides
    fun resetPasswordView(frag: ResetPasswordFragment): ResetPasswordView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: ResetPasswordFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}