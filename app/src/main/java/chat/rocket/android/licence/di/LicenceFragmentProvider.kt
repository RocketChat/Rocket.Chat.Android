package chat.rocket.android.licence.di

import chat.rocket.android.licence.ui.LicenceFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LicenceFragmentProvider {

    @ContributesAndroidInjector
    abstract fun provideLicenceFragment(): LicenceFragment
}