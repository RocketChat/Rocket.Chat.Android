package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.common.model.Token
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.AUTH_TOKEN
import testConfig.Config.Companion.CURRENT_SERVER
import testConfig.Config.Companion.UPDATED_AVATAR
import testConfig.Config.Companion.USERNAME
import testConfig.Config.Companion.USER_ID


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

    private val account = Account(
        CURRENT_SERVER, CURRENT_SERVER, null, null,
        USERNAME, UPDATED_AVATAR, null, null
    )

    private val token = Token(
        AUTH_TOKEN, USER_ID
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(CURRENT_SERVER)
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
        verify(navigator).toLogin(CURRENT_SERVER)
    }

    @Test
    fun `check account is saved`() {
        val method1 = loginOptionsPresenter.javaClass.getDeclaredMethod("setupConnectionInfo", String::class.java)
        method1.isAccessible = true
        val parameters1 = arrayOfNulls<Any>(1)
        parameters1[0] = CURRENT_SERVER
        method1.invoke(loginOptionsPresenter, *parameters1)
        val method = loginOptionsPresenter.javaClass.getDeclaredMethod("saveAccount", String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = USERNAME
        method.invoke(loginOptionsPresenter, *parameters)
        verify(saveAccountInteractor).save(account)
    }

    @Test
    fun `setup connection info`() {
        val method = loginOptionsPresenter.javaClass.getDeclaredMethod("setupConnectionInfo", String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = CURRENT_SERVER
        method.invoke(loginOptionsPresenter, *parameters)
        assertEquals(parameters[0], CURRENT_SERVER)
    }

    @Test
    fun `check token is saved`() {
        val method = loginOptionsPresenter.javaClass.getDeclaredMethod("saveToken", Token::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = token
        method.invoke(loginOptionsPresenter, *parameters)
        verify(tokenRepository).save(CURRENT_SERVER, token)
    }
}