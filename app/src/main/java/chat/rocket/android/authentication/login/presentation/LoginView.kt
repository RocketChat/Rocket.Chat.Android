package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.LoadingView

interface LoginView : LoadingView {
    fun onLoginError(message: String?)
}