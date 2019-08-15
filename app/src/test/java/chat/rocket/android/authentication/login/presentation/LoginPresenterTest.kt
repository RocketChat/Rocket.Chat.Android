package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.common.model.Token
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER
import testConfig.Config.Companion.UPDATED_AVATAR
import testConfig.Config.Companion.USERNAME

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

    private val account = Account(
        CURRENT_SERVER, CURRENT_SERVER, null,
        null, USERNAME, UPDATED_AVATAR
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(CURRENT_SERVER)
        loginPresenter = LoginPresenter(
            view, strategy, navigator, tokenRepository, localRepository, settingsInteractor,
            analyticsManager, saveCurrentServer, saveAccountInteractor, factory, serverInteractor
        )
    }

    @Test
    fun `check account is saved`() {
        loginPresenter.setupView()
        val method = loginPresenter.javaClass.getDeclaredMethod("saveAccount", String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = USERNAME
        method.invoke(loginPresenter, *parameters)
        verify(saveAccountInteractor).save(account)
    }

    @Test
    fun `view should not be null`() {
        loginPresenter.setupView()
        assertNotNull(view)
    }

    @Test
    fun `save token to repository`() {
        loginPresenter.setupView()
        val method = loginPresenter.javaClass.getDeclaredMethod("saveToken", Token::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = token
        method.invoke(loginPresenter, *parameters)
        verify(tokenRepository).save(CURRENT_SERVER, token)
    }

    @Test
    fun `navigate to forgot password`() {
        loginPresenter.forgotPassword()
        verify(navigator).toForgotPassword()
    }
}