package chat.rocket.android.main.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.viewmodel.NavHeaderViewModel
import chat.rocket.android.main.viewmodel.NavHeaderViewModelMapper
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.unregisterPushToken
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(
        private val view: MainView,
        private val strategy: CancelStrategy,
        private val navigator: MainNavigator,
        private val tokenRepository: TokenRepository,
        private val serverInteractor: GetCurrentServerInteractor,
        private val localRepository: LocalRepository,
        private val navHeaderMapper: NavHeaderViewModelMapper,
        private val saveAccountInteractor: SaveAccountInteractor,
        private val getAccountsInteractor: GetAccountsInteractor,
        private val removeAccountInterector: RemoveAccountInterector,
        private val factory: RocketChatClientFactory,
        getSettingsInteractor: GetSettingsInteractor,
        managerFactory: ConnectionManagerFactory
) : CheckServerPresenter(strategy, client = factory.create(serverInteractor.get()!!), view = view) {
    private val currentServer = serverInteractor.get()!!
    private val manager = managerFactory.create(currentServer)
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = getSettingsInteractor.get(serverInteractor.get()!!)

    fun toChatList() = navigator.toChatList()

    fun toUserProfile() = navigator.toUserProfile()

    fun toSettings() = navigator.toSettings()

    fun loadCurrentInfo() {
        checkServerInfo()
        launchUI(strategy) {
            try {
                val me = client.me()
                val model = navHeaderMapper.mapToViewModel(me)

                saveAccount(model)
                view.setupNavHeader(model, getAccountsInteractor.get())
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> {
                        logout()
                    }
                    else -> {
                        Timber.d(ex, "Error loading my information for navheader")
                        ex.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveAccount(me: NavHeaderViewModel) {
        val icon = settings.favicon()?.let {
            UrlHelper.getServerLogoUrl(currentServer, it)
        }
        val account = Account(currentServer, icon, me.serverLogo, me.username, me.avatar)
        saveAccountInteractor.save(account)
    }

    /**
     * Logout from current server.
     */
    fun logout() {
        launchUI(strategy) {
            try {
                clearTokens()
                client.logout()
            } catch (exception: RocketChatException) {
                Timber.d(exception, "Error calling logout")
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }

            try {
                disconnect()
                removeAccountInterector.remove(currentServer)
                tokenRepository.remove(currentServer)
                navigator.toNewServer()
            } catch (ex: Exception) {
                Timber.d(ex, "Error cleaning up the session...")
            }

            navigator.toNewServer()
        }
    }

    private suspend fun clearTokens() {
        serverInteractor.clear()
        val pushToken = localRepository.get(LocalRepository.KEY_PUSH_TOKEN)
        if (pushToken != null) {
            client.unregisterPushToken(pushToken)
        }
        localRepository.clearAllFromServer(currentServer)
    }

    fun connect() {
        manager.connect()
    }

    fun disconnect() {
        manager.disconnect()
    }

    fun changeServer(serverUrl: String) {
        if (currentServer != serverUrl) {
            navigator.toNewServer(serverUrl)
        } else {
            view.closeServerSelection()
        }
    }

    fun addNewServer() {
        navigator.toServerScreen()
    }

    suspend fun refreshToken(token: String?) {
        token?.let {
            client.registerPushToken(it, getAccountsInteractor.get(), factory)
        }
    }
}