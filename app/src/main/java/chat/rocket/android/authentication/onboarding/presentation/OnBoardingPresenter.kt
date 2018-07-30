package chat.rocket.android.authentication.onboarding.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import javax.inject.Inject

class OnBoardingPresenter @Inject constructor(
        private val view: OnBoardingView,
        private val strategy: CancelStrategy,
        private val navigator: AuthenticationNavigator
) {
    fun createServer(){
        navigator.toWebPage("https://cloud.rocket.chat/trial")
    }
}