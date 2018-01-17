package chat.rocket.android.authentication.presentation

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.common.model.Token
import chat.rocket.core.TokenRepository
import javax.inject.Inject

class AuthenticationPresenter @Inject constructor(private val navigator: AuthenticationNavigator,
                                                  private val getCurrentServerInteractor: GetCurrentServerInteractor,
                                                  private val multiServerRepository: MultiServerTokenRepository,
                                                  private val settingsRepository: SettingsRepository,
                                                  private val tokenRepository: TokenRepository) {

    fun loadCredentials(callback: (authenticated: Boolean) -> Unit) {
        val currentServer = getCurrentServerInteractor.get()
        val serverToken = currentServer?.let { multiServerRepository.get(currentServer) }
        val settings = currentServer?.let { settingsRepository.get(currentServer) }

        if (currentServer == null || serverToken == null || settings == null) {
            callback(false)
        } else {
            tokenRepository.save(Token(serverToken.userId, serverToken.authToken))
            callback(true)
            navigator.toChatList()
        }
    }
}