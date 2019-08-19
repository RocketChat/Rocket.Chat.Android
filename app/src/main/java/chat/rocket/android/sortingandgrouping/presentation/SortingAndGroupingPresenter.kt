package chat.rocket.android.sortingandgrouping.presentation

import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import javax.inject.Inject
import javax.inject.Named

class SortingAndGroupingPresenter @Inject constructor(
    private val view: SortingAndGroupingView,
    private val sortingAndGroupingInteractor: SortingAndGroupingInteractor,
    @Named("currentServer") private val currentServerUrl: String?
) {

    fun getSortingAndGroupingPreferences() {
        currentServerUrl?.let {
            with(sortingAndGroupingInteractor) {
                view.showSortingAndGroupingPreferences(
                    getSortByName(it),
                    getUnreadOnTop(it),
                    getGroupByType(it),
                    getGroupByFavorites(it)
                )
            }
        }
    }

    fun saveSortingAndGroupingPreferences(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        currentServerUrl?.let {
            sortingAndGroupingInteractor.save(
                it,
                isSortByName,
                isUnreadOnTop,
                isGroupByType,
                isGroupByFavorites
            )
        }
    }
}