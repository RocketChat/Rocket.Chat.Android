package chat.rocket.android.wallet.transaction.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.wallet.transaction.presentation.TransactionView
import chat.rocket.android.wallet.transaction.ui.TransactionFragment
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.experimental.Job

@Module
class TransactionFragmentModule {

    @Provides
    @PerFragment
    fun provideJob() = Job()

    @Provides
    @PerFragment
    fun transactionView(frag: TransactionFragment): TransactionView {
        return frag
    }

    @Provides
    @PerFragment
    fun settingsLifecycleOwner(frag: TransactionFragment): LifecycleOwner {
        return frag
    }

    @Provides
    @PerFragment
    fun provideCancelStrategy(owner: LifecycleOwner, jobs: Job): CancelStrategy {
        return CancelStrategy(owner, jobs)
    }
}