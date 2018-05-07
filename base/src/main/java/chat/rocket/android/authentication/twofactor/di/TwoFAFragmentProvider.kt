package chat.rocket.android.authentication.twofactor.di

import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module abstract class TwoFAFragmentProvider {

    @ContributesAndroidInjector(modules = [TwoFAFragmentModule::class])
    abstract fun provideTwoFAFragment(): TwoFAFragment
}
