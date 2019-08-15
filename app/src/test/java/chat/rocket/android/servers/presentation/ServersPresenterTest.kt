package chat.rocket.android.servers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.GetAccountsInteractor
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER

class ServersPresenterTest {

    private val view = Mockito.mock(ServersView::class.java)
    private val navigator = Mockito.mock(MainNavigator::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val getAccountsInteractor = Mockito.mock(GetAccountsInteractor::class.java)

    lateinit var serversPresenter: ServersPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        serversPresenter = ServersPresenter(
            view, navigator, strategy, getAccountsInteractor, CURRENT_SERVER
        )
    }

    @Test
    fun `navigate to server screen if new server is added`() {
        serversPresenter.addNewServer()
        verify(navigator).toServerScreen()
    }

    @Test
    fun `hide server view if serverUrl equal curentServer`() {
        serversPresenter.changeServer(CURRENT_SERVER)
        verify(view).hideServerView()
    }

    @Test
    fun `switch Or add new server if serverUrl not equal curentServer`() {
        serversPresenter.changeServer("")
        verify(navigator).switchOrAddNewServer("")
    }
}