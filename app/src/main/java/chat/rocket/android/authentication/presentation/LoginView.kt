package chat.rocket.android.authentication.presentation

interface LoginView {
    fun showProgress()
    fun hideProgress()
    fun onLoginError(message: String?)
}