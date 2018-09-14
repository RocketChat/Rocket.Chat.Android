package chat.rocket.android.authentication.loginoptions.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsView
import chat.rocket.android.authentication.loginoptions.ui.LoginOptionsFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class LoginOptionsFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun loginOptionsView(frag: LoginOptionsFragment): LoginOptionsView{
        return frag
    }

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: LoginOptionsFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}