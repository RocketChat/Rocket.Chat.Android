package chat.rocket.android.authentication.signup.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class SignupFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun signupView(frag: SignupFragment): SignupView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: SignupFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}