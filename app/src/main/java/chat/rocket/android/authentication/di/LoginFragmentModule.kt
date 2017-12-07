package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.presentation.LoginView
import chat.rocket.android.authentication.ui.LoginFragment
import dagger.Module
import dagger.Provides

@Module
class LoginFragmentModule {
    @Provides
    fun loginView(frag: LoginFragment): LoginView {
        return frag
    }
}
