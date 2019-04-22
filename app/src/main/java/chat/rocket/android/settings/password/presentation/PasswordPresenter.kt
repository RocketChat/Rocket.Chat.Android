package chat.rocket.android.settings.password.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.updateProfile
import javax.inject.Inject

class PasswordPresenter @Inject constructor(
    private val view: PasswordView,
    private val strategy: CancelStrategy,
    private val analyticsManager: AnalyticsManager,
    private val userHelp: UserHelper,
    serverInteractor: GetCurrentServerInteractor,
    factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)

    fun updatePassword(password: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                userHelp.user()?.id?.let { userId ->
                    retryIO("updateProfile()") {
                        client.updateProfile(userId, null, null, password, null)
                    }
                    analyticsManager.logResetPassword(true)
                    view.showPasswordSuccessfullyUpdatedMessage()
                }
            } catch (exception: RocketChatException) {
                analyticsManager.logResetPassword(false)
                exception.message?.let { errorMessage ->
                    view.showPasswordFailsUpdateMessage(errorMessage)
                }
            } finally {
                view.hideLoading()
            }
        }
    }
}