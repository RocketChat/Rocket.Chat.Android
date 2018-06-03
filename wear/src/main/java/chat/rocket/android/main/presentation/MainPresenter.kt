package chat.rocket.android.main.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.TokenRepository
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val tokenRepository: TokenRepository
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
}