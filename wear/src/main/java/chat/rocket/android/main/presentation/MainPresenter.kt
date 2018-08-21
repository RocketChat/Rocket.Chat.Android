package chat.rocket.android.main.presentation

import chat.rocket.android.main.ui.MainNavigator
import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.LocalRepository
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.server.TokenRepository
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.registerPushToken
import chat.rocket.core.internal.rest.unregisterPushToken
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val view: MainView,
    private val serverInteractor: GetCurrentServerInteractor,
    private val localRepository: LocalRepository,
    private val factory: RocketChatClientFactory,
    private val tokenRepository: TokenRepository,
    private val navigator: MainNavigator
) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)

    suspend fun refreshToken(token: String?) {
        launch {
            token?.let {
                localRepository.save(LocalRepository.KEY_PUSH_TOKEN, token)
                try {
                    retryIO(description = "register push token for $currentServer") {
                        client.registerPushToken(it)
                    }
                } catch (ex: Exception) {
                    Timber.d(ex, "Error registering Push token for $currentServer")
                    ex.printStackTrace()
                }
            }
        }
    }

    fun logout() {
        launch {
            try {
                clearTokens()
                retryIO("logout") { client.logout() }
            } catch (exception: RocketChatException) {
                Timber.d(exception, "Error calling logout")
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
            try {
                tokenRepository.remove(currentServer)
                navigator.toStarterActivity()
            } catch (ex: Exception) {
                Timber.d(ex, "Error cleaning up the session...")
            }
        }
    }

    private suspend fun clearTokens() {
        serverInteractor.clear()
        val pushToken = localRepository.get(LocalRepository.KEY_PUSH_TOKEN)
        if (pushToken != null) {
            try {
                retryIO("unregisterPushToken") { client.unregisterPushToken(pushToken) }
                view.invalidateToken(pushToken)
            } catch (ex: Exception) {
                Timber.d(ex, "Error unregistering push token")
            }
        }
        localRepository.clearAllFromServer(currentServer)
    }
}