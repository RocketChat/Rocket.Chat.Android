package chat.rocket.android.wallet.transaction.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.wallet.transaction.ui.TransactionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TransactionFragmentProvider {
    @ContributesAndroidInjector(modules = [TransactionFragmentModule::class])
    @PerFragment
    abstract fun provideTransactionFragment(): TransactionFragment
}