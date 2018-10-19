package chat.rocket.android.authentication.login.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class LoginFragmentModule {

    @Provides
    @PerFragment
    fun loginView(frag: LoginFragment): LoginView = frag

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: LoginFragment): LifecycleOwner = frag
}