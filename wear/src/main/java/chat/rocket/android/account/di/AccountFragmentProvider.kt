package chat.rocket.android.account.di

import chat.rocket.android.account.ui.AccountFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AccountFragmentProvider {

    @ContributesAndroidInjector(modules = [AccountFragmentModule::class])
    @PerFragment
    abstract fun provideAccountFragment(): AccountFragment
}