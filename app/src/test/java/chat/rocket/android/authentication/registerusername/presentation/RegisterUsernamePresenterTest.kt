package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.Config.Companion.currentServer
import chat.rocket.android.authentication.Config.Companion.userAvatar
import chat.rocket.android.authentication.Config.Companion.userName
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class RegisterUsernamePresenterTest {

    private val view = mock(RegisterUsernameView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val tokenRepository = mock(TokenRepository::class.java)
    private val settingsInteractor = mock(GetSettingsInteractor::class.java)
    private val analyticsManager = mock(AnalyticsManager::class.java)
    private val saveCurrentServer = mock(SaveCurrentServerInteractor::class.java)
    private val saveAccountInteractor = mock(SaveAccountInteractor::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)
    private var settings: PublicSettings = settingsInteractor.get(currentServer)
    private val token = tokenRepository.get(currentServer)

    private lateinit var registerUsernamePresenter: RegisterUsernamePresenter

    private val account = Account(
        currentServer, currentServer, null,
        null, userName, userAvatar
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        registerUsernamePresenter = RegisterUsernamePresenter(
            view, strategy, navigator, tokenRepository, saveAccountInteractor, analyticsManager,
            saveCurrentServer, serverInteractor, factory, settingsInteractor
        )
    }

    @Test
    fun check_save_account() {
        registerUsernamePresenter.saveAccount(userName)
        verify(saveAccountInteractor).save(account)
    }
}