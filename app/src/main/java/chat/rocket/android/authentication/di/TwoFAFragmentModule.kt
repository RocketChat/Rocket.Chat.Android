package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.presentation.LoginView
import chat.rocket.android.authentication.presentation.TwoFAView
import chat.rocket.android.authentication.ui.LoginFragment
import chat.rocket.android.authentication.ui.TwoFAFragment
import dagger.Module
import dagger.Provides

@Module
class TwoFAFragmentModule {
    @Provides
    fun loginView(frag: TwoFAFragment): TwoFAView {
        return frag
    }
}
