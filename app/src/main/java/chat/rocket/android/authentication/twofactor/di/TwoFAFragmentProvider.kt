package chat.rocket.android.authentication.twofactor.di

import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TwoFAFragmentProvider {

    @ContributesAndroidInjector(modules = [TwoFAFragmentModule::class])
    @PerFragment
    abstract fun provideTwoFAFragment(): TwoFAFragment
}
