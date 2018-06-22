package chat.rocket.android.starter.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.SaveCurrentServerInteractor
import chat.rocket.android.server.TokenRepository
import chat.rocket.common.model.Token
import javax.inject.Inject

class StarterActivityPresenter @Inject constructor(
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val tokenRepository: TokenRepository,
    private val serverInteractor: SaveCurrentServerInteractor
) {
    fun loadCredentials(callback: (authenticated: Boolean) -> Unit) {
        val currentServer = getCurrentServerInteractor.get()
        val serverToken = currentServer?.let { tokenRepository.get(it) }
        if (currentServer == null || serverToken == null) {
            callback(false)
        } else {
            callback(true)
        }
    }

    //TODO remove this later on, this is just for testing purposes until data layer interaction feature does not work
    fun saveCredentials() {
        val currentServer = "https://open.rocket.chat"
        val loginToken = Token("MaBK2dquY8TbaH6d3", "PpMOm_4ymeEDGjpGIq0HOp6rbSfsZ1SJ8hdBM68WmHe")
        serverInteractor.save(currentServer)
        tokenRepository.save(currentServer, loginToken)
    }
}