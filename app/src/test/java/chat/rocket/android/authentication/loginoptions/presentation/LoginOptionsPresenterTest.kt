package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config
import testConfig.Config.Companion.CAS_TOKEN
import testConfig.Config.Companion.OAUTH_SECRET
import testConfig.Config.Companion.OAUTH_TOKEN
import testConfig.Config.Companion.SAML_TOKEN
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

    private val deepLinkInfo = DeepLinkInfo(
        "www.abc.com", "UserId", "token",
        "rId", "public", "abc"
    )

    private val account = Account(
        currentServer, currentServer, null,
        null, Config.USERNAME, Config.UPDATED_AVATAR
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(strategy.isTest).thenReturn(true)
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

    @Test
    fun `check account is saved`() {
        loginOptionsPresenter.authenticateWithOauth(OAUTH_TOKEN, OAUTH_SECRET)
        val method = loginOptionsPresenter.javaClass.getDeclaredMethod("saveAccount", String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = Config.USERNAME
        method.invoke(loginOptionsPresenter, *parameters)
        verify(saveAccountInteractor).save(account)
    }

    @Test
    fun `setup connection info`() {
        val method = loginOptionsPresenter.javaClass.getDeclaredMethod("setupConnectionInfo", String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = currentServer
        method.invoke(loginOptionsPresenter, *parameters)
        assertEquals(parameters[0], currentServer)
    }

    @Test
    fun `authenticate user with Oauth`() {
        val result = loginOptionsPresenter.authenticateWithOauth(OAUTH_TOKEN, OAUTH_SECRET)
        assertEquals(result ,Unit)
    }

    @Test
    fun `authenticate user with Cas`() {
        val result = loginOptionsPresenter.authenticateWithCas(CAS_TOKEN)
        assertEquals(result ,Unit)
    }

    @Test
    fun `authenticate user with Saml`() {
        val result = loginOptionsPresenter.authenticateWithSaml(SAML_TOKEN)
        assertEquals(result ,Unit)
    }

    @Test
    fun `authenticate user with Deeplink`() {
        val result = loginOptionsPresenter.authenticateWithDeepLink(deepLinkInfo)
        assertEquals(result ,Unit)
    }
}