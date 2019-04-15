package chat.rocket.android.servers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.util.ifNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class ServersPresenter @Inject constructor(
    private val view: ServersView,
    private val navigator: MainNavigator,
    private val strategy: CancelStrategy,
    private val getAccountsInteractor: GetAccountsInteractor,
    @Named("currentServer") private val currentServerUrl: String
) {

    fun getAllServers() {
        launchUI(strategy) {
            try {
                view.showServerList(getAccountsInteractor.get(), currentServerUrl)
            } catch (exception: Exception) {
                Timber.e(exception, "Error loading servers")
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun changeServer(serverUrl: String) {
        if (currentServerUrl != serverUrl) {
            navigator.switchOrAddNewServer(serverUrl)
        } else {
            view.hideServerView()
        }
    }

    fun addNewServer() {
        view.hideServerView()
        navigator.toServerScreen()
    }
}