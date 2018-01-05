package chat.rocket.android.authentication.signup.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class SignupFragmentModule {

    @Provides
    fun signupView(frag: SignupFragment): SignupView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: SignupFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}