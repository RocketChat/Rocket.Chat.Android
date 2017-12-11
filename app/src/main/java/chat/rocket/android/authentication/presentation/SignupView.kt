package chat.rocket.android.authentication.presentation

interface SignupView {
    fun showProgress()
    fun hideProgress()
    fun onSignupError(message: String? = "Unknown error")
}