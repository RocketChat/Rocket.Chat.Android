package chat.rocket.android.privacy.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.privacy.ui.PrivacyFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PrivacyFragmentProvider {

    @ContributesAndroidInjector(modules = [PrivacyFragmentModule::class])
    @PerFragment
    abstract fun providePrivacyFragment(): PrivacyFragment
}