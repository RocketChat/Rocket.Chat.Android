package chat.rocket.android.authentication.di

import chat.rocket.android.authentication.presentation.SignupView
import chat.rocket.android.authentication.ui.SignupFragment
import dagger.Module
import dagger.Provides

@Module
class SignupFragmentModule {
    @Provides
    fun signupView(frag: SignupFragment): SignupView {
        return frag
    }
}
