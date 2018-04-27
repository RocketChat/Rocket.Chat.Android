package chat.rocket.android.authentication.resetpassword.di

import chat.rocket.android.authentication.resetpassword.ui.ResetPasswordFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ResetPasswordFragmentProvider {

    @ContributesAndroidInjector(modules = [ResetPasswordFragmentModule::class])
    abstract fun provideResetPasswordFragment(): ResetPasswordFragment
}