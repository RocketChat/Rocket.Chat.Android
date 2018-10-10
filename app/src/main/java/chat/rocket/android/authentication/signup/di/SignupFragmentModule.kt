package chat.rocket.android.authentication.signup.di

import androidx.lifecycle.LifecycleOwner
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.dagger.scope.PerFragment
import dagger.Module
import dagger.Provides

@Module
class SignupFragmentModule {

    @Provides
    @PerFragment
    fun signupView(frag: SignupFragment): SignupView = frag

    @Provides
    @PerFragment
    fun provideLifecycleOwner(frag: SignupFragment): LifecycleOwner = frag
}