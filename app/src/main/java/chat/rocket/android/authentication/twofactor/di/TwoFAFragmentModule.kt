package chat.rocket.android.authentication.twofactor.di

import android.arch.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.twofactor.presentation.TwoFAView
import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
@PerFragment
class TwoFAFragmentModule {

    @Provides
    fun loginView(frag: TwoFAFragment): TwoFAView {
        return frag
    }

    @Provides
    fun provideLifecycleOwner(frag: TwoFAFragment): LifecycleOwner {
        return frag
    }

    @Provides
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}
