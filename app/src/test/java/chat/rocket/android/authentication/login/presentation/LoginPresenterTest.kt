package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.common.model.Token
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.currentServer

class LoginPresenterTest {

    lateinit var loginPresenter: LoginPresenter

    private val view = mock(LoginView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val tokenRepository = mock(TokenRepository::class.java)
    private val localRepository = mock(LocalRepository::class.java)
    private val settingsInteractor = mock(GetSettingsInteractor::class.java)
    private val analyticsManager = mock(AnalyticsManager::class.java)
    private val saveCurrentServer = mock(SaveCurrentServerInteractor::class.java)
    private val saveAccountInteractor = mock(SaveAccountInteractor::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)
    private val token = mock(Token::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        loginPresenter = LoginPresenter(
            view, strategy, navigator, tokenRepository, localRepository, settingsInteractor,
            analyticsManager, saveCurrentServer, saveAccountInteractor, factory, serverInteractor
        )
    }

    @Test
    fun `view should not be null`() {
        loginPresenter.setupView()
        assertNotNull(view)
    }

    @Test
    fun `save token to repository`() {
        saveToken(token)
        verify(tokenRepository).save(currentServer, token)
    }

    private fun saveToken(token: Token) = tokenRepository.save(currentServer, token)
}