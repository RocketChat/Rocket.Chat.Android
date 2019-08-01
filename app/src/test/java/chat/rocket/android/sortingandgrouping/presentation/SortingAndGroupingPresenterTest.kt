package chat.rocket.android.sortingandgrouping.presentation

import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.currentServer

class SortingAndGroupingPresenterTest {

    private val view = Mockito.mock(SortingAndGroupingView::class.java)
    private val sortingAndGroupingInteractor =
        Mockito.mock(SortingAndGroupingInteractor::class.java)

    lateinit var sortingAndGroupingPresenter: SortingAndGroupingPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sortingAndGroupingPresenter = SortingAndGroupingPresenter(
            view, sortingAndGroupingInteractor, currentServer
        )
    }

    @Test
    fun `get sorting and grouping preferences`() {
        sortingAndGroupingPresenter.getSortingAndGroupingPreferences()
        verify(view).showSortingAndGroupingPreferences(
            sortingAndGroupingInteractor.getSortByName(currentServer),
            sortingAndGroupingInteractor.getUnreadOnTop(currentServer),
            sortingAndGroupingInteractor.getGroupByType(currentServer),
            sortingAndGroupingInteractor.getGroupByFavorites(currentServer)
        )
    }

    @Test
    fun `save sorting and grouping preferences`() {
        sortingAndGroupingPresenter.saveSortingAndGroupingPreferences(true, false, false, false)
        verify(sortingAndGroupingInteractor).save(currentServer, true, false, false, false)
    }
}