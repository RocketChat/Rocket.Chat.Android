package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveCurrentServerInteractor
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.util.ifNull
import javax.inject.Inject

class ServerPresenter @Inject constructor(private val view: ServerView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator,
                                          private val serverInteractor: SaveCurrentServerInteractor,
                                          private val refreshSettingsInteractor: RefreshSettingsInteractor) {
    fun connect(server: String) {
        if (!UrlHelper.isValidUrl(server)) {
            view.showInvalidServerUrlMessage()
        } else {
            launchUI(strategy) {
                if (NetworkHelper.hasInternetAccess()) {
                    view.showLoading()
                    try {
                        refreshSettingsInteractor.refresh(server)
                        serverInteractor.save(server)
                        navigator.toLogin()
                    } catch (ex: Exception) {
                        ex.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    } finally {
                        view.hideLoading()
                    }
                } else {
                    view.showNoInternetConnection()
                }
            }
        }
    }
}