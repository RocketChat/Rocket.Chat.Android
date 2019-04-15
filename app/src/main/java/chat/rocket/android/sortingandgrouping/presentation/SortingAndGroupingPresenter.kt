package chat.rocket.android.sortingandgrouping.presentation

import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import javax.inject.Inject
import javax.inject.Named

class SortingAndGroupingPresenter @Inject constructor(
    private val view: SortingAndGroupingView,
    private val sortingAndGroupingInteractor: SortingAndGroupingInteractor,
    @Named("currentServer") private val currentServerUrl: String
) {

    fun getSortingAndGroupingPreferences() {
        with(sortingAndGroupingInteractor) {
            view.showSortingAndGroupingPreferences(
                getSortByName(currentServerUrl),
                getUnreadOnTop(currentServerUrl),
                getGroupByType(currentServerUrl),
                getGroupByFavorites(currentServerUrl)
            )
        }
    }

    fun saveSortingAndGroupingPreferences(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        sortingAndGroupingInteractor.save(
            currentServerUrl,
            isSortByName,
            isUnreadOnTop,
            isGroupByType,
            isGroupByFavorites
        )
    }
}