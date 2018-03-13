package chat.rocket.android.main.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.disconnect
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.unregisterPushToken
import javax.inject.Inject

class MainPresenter @Inject constructor(private val view: MainView,
                                        private val strategy: CancelStrategy,
                                        private val navigator: MainNavigator,
                                        private val serverInteractor: GetCurrentServerInteractor,
                                        private val localRepository: LocalRepository,
                                        managerFactory: ConnectionManagerFactory,
                                        factory: RocketChatClientFactory) {
    private val currentServer = serverInteractor.get()!!
    private val manager = managerFactory.create(currentServer)
    private val client: RocketChatClient = factory.create(currentServer)

    fun toChatList() = navigator.toChatList()

    fun toUserProfile() = navigator.toUserProfile()

    fun toSettings() = navigator.toSettings()

    /**
     * Logout from current server.
     */
    fun logout() {
        launchUI(strategy) {
            try {
                clearTokens()
                client.logout()
                //TODO: Add the code to unsubscribe to all subscriptions.
                client.disconnect()
                view.onLogout()
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    private suspend fun clearTokens() {
        serverInteractor.clear()
        val pushToken = localRepository.get(LocalRepository.KEY_PUSH_TOKEN)
        if (pushToken != null) {
            client.unregisterPushToken(pushToken)
            localRepository.clear(LocalRepository.KEY_PUSH_TOKEN)
        }
        localRepository.clearAllFromServer(currentServer)
    }

    fun connect() {
        manager.connect()
    }

    fun disconnect() {
        manager.disconnect()
    }
}