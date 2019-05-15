package chat.rocket.android.server.domain

interface SortingAndGroupingRepository {

    fun save(
        currentServerUrl: String,
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    )

    fun getSortByName(currentServerUrl: String): Boolean

    fun getUnreadOnTop(currentServerUrl: String): Boolean

    fun getGroupByType(currentServerUrl: String): Boolean

    fun getGroupByFavorites(currentServerUrl: String): Boolean
}