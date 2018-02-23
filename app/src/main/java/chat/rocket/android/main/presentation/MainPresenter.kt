package chat.rocket.android.main.presentation

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.disconnect
import chat.rocket.core.internal.rest.unregisterPushToken
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(private val navigator: MainNavigator,
                                        private val serverInteractor: GetCurrentServerInteractor,
                                        private val localRepository: LocalRepository,
                                        factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)
    private val currentServer = serverInteractor.get()!!

    fun toChatList() = navigator.toChatList()

    fun toUserProfile() = navigator.toUserProfile()

    /**
     * Logout from current server.
     */
    fun logout() {
        // TODO: inject CancelStrategy, and MainView.
//        launchUI(strategy) {
            try {
//                clearTokens()
//                client.logout()
                //TODO: Add the code to unsubscribe to all subscriptions.
                client.disconnect()
//                view.onLogout()
            } catch (e: RocketChatException) {
                Timber.e(e)
//                view.showMessage(e.message!!)
            }
//        }
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
}