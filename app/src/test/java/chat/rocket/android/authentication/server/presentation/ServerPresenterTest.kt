package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.Config.Companion.invalidServer
import chat.rocket.android.authentication.Config.Companion.validServer
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.common.model.Token
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


class ServerPresenterTest {
    private val view = Mockito.mock(ServerView::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(AuthenticationNavigator::class.java)
    private val refreshSettingsInteractor = Mockito.mock(RefreshSettingsInteractor::class.java)
    private val getAccountsInteractor = Mockito.mock(GetAccountsInteractor::class.java)
    private val settingsInteractor = Mockito.mock(GetSettingsInteractor::class.java)
    private val analyticsManager = Mockito.mock(AnalyticsManager::class.java)
    private val serverInteractor = Mockito.mock(SaveConnectingServerInteractor::class.java)
    private val saveAccountInteractor = Mockito.mock(SaveAccountInteractor::class.java)
    private val factory = Mockito.mock(RocketChatClientFactory::class.java)
    private val token = Mockito.mock(Token::class.java)

    private lateinit var serverPresenter: ServerPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        serverPresenter = ServerPresenter(
            view, strategy, navigator, serverInteractor, refreshSettingsInteractor,
            getAccountsInteractor, settingsInteractor, factory
        )
    }

    @Test
    fun check_server_with_valid_server_url() {
        serverPresenter.checkServer(validServer)
        verify(view).showLoading()
    }

    @Test
    fun check_server_with_invalid_server_url() {
        serverPresenter.checkServer(invalidServer)
        verify(view).showInvalidServerUrlMessage()
    }
}