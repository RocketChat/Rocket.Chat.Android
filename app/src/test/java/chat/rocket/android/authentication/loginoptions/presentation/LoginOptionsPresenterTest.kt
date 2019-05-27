package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.core.RocketChatClient
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


class LoginOptionsPresenterTest {

    private var currentServer = "https://open.rocket.chat"
    private val serverUrl = "serverUrl"
    private val userName = "userName"
    private val avatar = "serverUrl/avatar/userName?format=jpeg&rc_uid=null&rc_token=null"

    private val account = Account(serverUrl, serverUrl, null,
        null, userName, avatar)

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
    private lateinit var settings: PublicSettings
    private val token = tokenRepository.get(currentServer)
    private lateinit var client: RocketChatClient


    lateinit var loginOptionsPresenter: LoginOptionsPresenter


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        loginOptionsPresenter = LoginOptionsPresenter(view, strategy, factory, navigator, settingsInteractor, localRepository,
            saveCurrentServerInteractor, saveAccountInteractor, analyticsManager, tokenRepository, serverInteractor)
    }

    @Test
    fun check_create_account() {
        loginOptionsPresenter.toCreateAccount()
        verify(navigator).toCreateAccount()
    }

    @Test
    fun check_login_with_email() {
        loginOptionsPresenter.toLoginWithEmail()
        verify(navigator).toLogin(currentServer)
    }

    @Test
    fun check_authentication_with_oAuth() {

    }

    @Test
    fun check_authentication_with_Cas() {

    }

    @Test
    fun check_authentication_with_Saml() {

    }

    @Test
    fun check_authentication_with_deep_link() {

    }

    @Test
    fun check_setup_connection_info() {

    }

    @Test
    fun check_account_is_saved() {
        loginOptionsPresenter.setupConnectionInfo(serverUrl)
        loginOptionsPresenter.saveAccount(userName)
        verify(saveAccountInteractor).save(account)
    }
}