package chat.rocket.android.authentication.resetpassword.di

import chat.rocket.android.authentication.resetpassword.ui.ResetPasswordFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ResetPasswordFragmentProvider {

    @ContributesAndroidInjector(modules = [ResetPasswordFragmentModule::class])
    @PerFragment
    abstract fun provideResetPasswordFragment(): ResetPasswordFragment
}