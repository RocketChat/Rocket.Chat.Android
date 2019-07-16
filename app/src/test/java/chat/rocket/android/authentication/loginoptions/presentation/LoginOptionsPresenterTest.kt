package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.currentServer


class LoginOptionsPresenterTest {

    private val view = Mockito.mock(LoginOptionsView::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(AuthenticationNavigator::class.java)
    private val localRepository = Mockito.mock(LocalRepository::class.java)
    private val settingsInteractor = Mockito.mock(GetSettingsInteractor::class.java)
    private val analyticsManager = Mockito.mock(AnalyticsManager::class.java)
    private val saveCurrentServerInteractor = Mockito.mock(SaveCurrentServerInteractor::class.java)
    private val saveAccountInteractor = Mockito.mock(SaveAccountInteractor::class.java)
    private val factory = Mockito.mock(RocketChatClientFactory::class.java)
    private val serverInteractor = Mockito.mock(GetConnectingServerInteractor::class.java)
    private val tokenRepository = Mockito.mock(TokenRepository::class.java)

    lateinit var loginOptionsPresenter: LoginOptionsPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        loginOptionsPresenter = LoginOptionsPresenter(
            view, strategy, factory, navigator, settingsInteractor, localRepository, saveCurrentServerInteractor,
            saveAccountInteractor, analyticsManager, tokenRepository, serverInteractor
        )
    }

    @Test
    fun `navigate to create account`() {
        loginOptionsPresenter.toCreateAccount()
        verify(navigator).toCreateAccount()
    }

    @Test
    fun `navigate to login with email`() {
        loginOptionsPresenter.toLoginWithEmail()
        verify(navigator).toLogin(currentServer)
    }
}