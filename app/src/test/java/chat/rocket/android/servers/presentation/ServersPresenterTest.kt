package chat.rocket.android.servers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.GetAccountsInteractor
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.currentServer


class ServersPresenterTest {

    private val view = Mockito.mock(ServersView::class.java)
    private val navigator = Mockito.mock(MainNavigator::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val getAccountsInteractor = Mockito.mock(GetAccountsInteractor::class.java)

    lateinit var serversPresenter: ServersPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(strategy.isTest).thenReturn(true)
        serversPresenter = ServersPresenter(
            view, navigator, strategy, getAccountsInteractor, currentServer
        )
    }

    @Test
    fun `navigate to server screen if new server is added`() {
        serversPresenter.addNewServer()
        verify(navigator).toServerScreen()
    }

    @Test
    fun `hide server view if serverUrl equal curentServer`() {
        serversPresenter.changeServer(currentServer)
        verify(view).hideServerView()
    }

    @Test
    fun `switch Or add new server if serverUrl not equal curentServer`() {
        serversPresenter.changeServer("")
        verify(navigator).switchOrAddNewServer("")
    }

    @Test
    fun `get all servers`() {
        val result = serversPresenter.getAllServers()
        assertEquals(result, Unit)
    }
}