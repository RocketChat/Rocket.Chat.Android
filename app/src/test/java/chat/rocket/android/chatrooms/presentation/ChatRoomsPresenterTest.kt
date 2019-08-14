package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import chat.rocket.android.server.domain.siteName
import chat.rocket.android.server.infrastructure.ConnectionManager
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER

class ChatRoomsPresenterTest {

    private val view = Mockito.mock(ChatRoomsView::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(MainNavigator::class.java)
    private val sortingAndGroupingInteractor =
        Mockito.mock(SortingAndGroupingInteractor::class.java)
    private val dbManager = Mockito.mock(DatabaseManager::class.java)
    private val manager = Mockito.mock(ConnectionManager::class.java)
    private val localRepository = Mockito.mock(LocalRepository::class.java)
    private val userHelper = Mockito.mock(UserHelper::class.java)
    private val settingsRepository = Mockito.mock(SettingsRepository::class.java)

    lateinit var chatRoomsPresenter: ChatRoomsPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        chatRoomsPresenter = ChatRoomsPresenter(
            view, strategy, navigator, CURRENT_SERVER, sortingAndGroupingInteractor, dbManager,
            manager, localRepository, userHelper, settingsRepository
        )
    }

    @Test
    fun `navigate to create channel`() {
        chatRoomsPresenter.toCreateChannel()
        verify(navigator).toCreateChannel()
    }

    @Test
    fun `navigate to setting`() {
        chatRoomsPresenter.toSettings()
        verify(navigator).toSettings()
    }

    @Test
    fun `navigate to directory`() {
        chatRoomsPresenter.toDirectory()
        verify(navigator).toDirectory()
    }

    @Test
    fun `get current server name`() {
        chatRoomsPresenter.getCurrentServerName()
        val settings = CURRENT_SERVER.let { settingsRepository.get(it) }
        verify(view).setupToolbar(settings.siteName() ?: CURRENT_SERVER)
    }

    @Test
    fun `sorting and grouping preferences`() {
        chatRoomsPresenter.getSortingAndGroupingPreferences()
        verify(view).setupSortingAndGrouping(false, false, false, false)
    }
}