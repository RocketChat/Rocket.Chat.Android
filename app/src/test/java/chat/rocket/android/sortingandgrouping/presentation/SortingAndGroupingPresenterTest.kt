package chat.rocket.android.sortingandgrouping.presentation

import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER

class SortingAndGroupingPresenterTest {

    private val view = Mockito.mock(SortingAndGroupingView::class.java)
    private val sortingAndGroupingInteractor =
        Mockito.mock(SortingAndGroupingInteractor::class.java)

    lateinit var sortingAndGroupingPresenter: SortingAndGroupingPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sortingAndGroupingPresenter = SortingAndGroupingPresenter(
            view, sortingAndGroupingInteractor, CURRENT_SERVER
        )
    }

    @Test
    fun `get sorting and grouping preferences`() {
        sortingAndGroupingPresenter.getSortingAndGroupingPreferences()
        verify(view).showSortingAndGroupingPreferences(
            sortingAndGroupingInteractor.getSortByName(CURRENT_SERVER),
            sortingAndGroupingInteractor.getUnreadOnTop(CURRENT_SERVER),
            sortingAndGroupingInteractor.getGroupByType(CURRENT_SERVER),
            sortingAndGroupingInteractor.getGroupByFavorites(CURRENT_SERVER)
        )
    }

    @Test
    fun `save sorting and grouping preferences`() {
        sortingAndGroupingPresenter.saveSortingAndGroupingPreferences(true, false, false, false)
        verify(sortingAndGroupingInteractor).save(CURRENT_SERVER, true, false, false, false)
    }
}