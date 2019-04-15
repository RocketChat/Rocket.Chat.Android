package chat.rocket.android.server.domain

import javax.inject.Inject

class SortingAndGroupingInteractor @Inject constructor(val repository: SortingAndGroupingRepository) {

    fun save(
        currentServerUrl: String,
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) = repository.save(
        currentServerUrl,
        isSortByName,
        isUnreadOnTop,
        isGroupByType,
        isGroupByFavorites
    )

    fun getSortByName(currentServerUrl: String): Boolean =
        repository.getSortByName(currentServerUrl)

    fun getUnreadOnTop(currentServerUrl: String): Boolean =
        repository.getUnreadOnTop(currentServerUrl)

    fun getGroupByType(currentServerUrl: String): Boolean =
        repository.getGroupByType(currentServerUrl)

    fun getGroupByFavorites(currentServerUrl: String): Boolean =
        repository.getGroupByFavorites(currentServerUrl)
}