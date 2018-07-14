package chat.rocket.android.authentication.login.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class LoginFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun loginView(frag: LoginFragment): LoginView {
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: LoginFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}