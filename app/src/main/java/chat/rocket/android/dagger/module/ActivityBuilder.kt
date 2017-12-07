package chat.rocket.android.dagger.module

import chat.rocket.android.app.MainActivity
import chat.rocket.android.authentication.di.LoginFragmentProvider
import chat.rocket.android.authentication.di.AuthenticationModule
import chat.rocket.android.authentication.di.ServerFragmentProvider
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.dagger.scope.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @PerActivity
    @ContributesAndroidInjector(modules = arrayOf(
        AuthenticationModule::class,
        LoginFragmentProvider::class,
        ServerFragmentProvider::class
    ))
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity
}
