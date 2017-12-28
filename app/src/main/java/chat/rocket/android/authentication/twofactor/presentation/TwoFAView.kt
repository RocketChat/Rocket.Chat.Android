package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface TwoFAView : LoadingView, MessageView {

    /**
     * Alerts the user about a blank two factor authentication code.
     */
    fun alertBlankTwoFactorAuthenticationCode()
}