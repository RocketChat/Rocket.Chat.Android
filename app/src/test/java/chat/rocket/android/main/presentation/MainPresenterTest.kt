package chat.rocket.android.main.presentation

import chat.rocket.android.core.behaviours.AppLanguageView
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetCurrentLanguageInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CHAT_ROOM_ID
import testConfig.Config.Companion.CURRENT_SERVER

class MainPresenterTest {
    private val mainNavigator = Mockito.mock(MainNavigator::class.java)
    private val appLanguageView = Mockito.mock(AppLanguageView::class.java)
    private val refreshSettingsInteractor = Mockito.mock(RefreshSettingsInteractor::class.java)
    private val refreshPermissionsInteractor = Mockito.mock(RefreshPermissionsInteractor::class.java)
    private val connectionManagerFactory = Mockito.mock(ConnectionManagerFactory::class.java)
    private val getLanguageInteractor = Mockito.mock(GetCurrentLanguageInteractor::class.java)
    private val groupedPush = Mockito.mock(GroupedPush::class.java)

    lateinit var mainPresenter: MainPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mainPresenter = MainPresenter(
            CURRENT_SERVER, mainNavigator, appLanguageView, refreshSettingsInteractor,
            refreshPermissionsInteractor, connectionManagerFactory, getLanguageInteractor, groupedPush
        )
    }

    @Test
    fun connect() {
        mainPresenter.connect()
        verify(refreshSettingsInteractor).refreshAsync(CURRENT_SERVER)
        verify(refreshPermissionsInteractor).refreshAsync(CURRENT_SERVER)
        verify(connectionManagerFactory).create(CURRENT_SERVER)?.connect()
    }

    @Test
    fun `navigate to chatlist`(){
        mainPresenter.showChatList(CHAT_ROOM_ID, null)
        verify(mainNavigator).toChatList(CHAT_ROOM_ID, null)
    }

    @Test
    fun `update app language`() {
        `when`(getLanguageInteractor.getLanguage()).thenReturn("hi")
        `when`(getLanguageInteractor.getCountry()).thenReturn("rIN")
        mainPresenter.getAppLanguage()
        verify(getLanguageInteractor).getLanguage()
        verify(appLanguageView).updateLanguage("hi", "rIN")
    }

    @Test
    fun `clear chatroom notifications`() {
        val result = mainPresenter.clearNotificationsForChatRoom(null)
        assertEquals(result, Unit)
    }
}