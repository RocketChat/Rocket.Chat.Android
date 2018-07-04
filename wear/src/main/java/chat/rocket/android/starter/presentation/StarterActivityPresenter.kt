package chat.rocket.android.starter.presentation

import chat.rocket.android.server.*
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.Token
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class StarterActivityPresenter @Inject constructor(
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val tokenRepository: TokenRepository,
    private val factory: RocketChatClientFactory,
    private val localRepository: LocalRepository,
    private val serverInteractor: SaveCurrentServerInteractor
) {
    private lateinit var client: RocketChatClient
    private var username: String? = null
    fun loadCredentials(callback: (authenticated: Boolean) -> Unit) {
        val currentServer = getCurrentServerInteractor.get()
        val serverToken = currentServer?.let { tokenRepository.get(it) }

        client = factory.create(currentServer!!)
        launch {
            username = retryIO("me()") {
                client.me().username
            }
            if (username != null) {
                localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, username)
            }
            if (currentServer == null || serverToken == null) {
                callback(false)
            } else {
                callback(true)
            }
        }
    }

    //TODO remove this later on, this is just for testing purposes until data layer interaction feature does not work
    fun saveCredentials() {
        val currentServer = "https://open.rocket.chat"
        val loginToken = Token("userId", "token")
        serverInteractor.save(currentServer)
        tokenRepository.save(currentServer, loginToken)
    }
}