package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.core.behaviours.LoadingView

interface SignupView : LoadingView {
    fun onSignupError(message: String? = "Unknown error")
}