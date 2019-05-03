package chat.rocket.android.server.infrastructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.SortingAndGroupingRepository

private const val SORT_BY_NAME_KEY = "SORT_BY_NAME_KEY"
private const val UNREAD_ON_TOP_KEY = "UNREAD_ON_TOP_KEY"
private const val GROUP_BY_TYPE_KEY = "GROUP_BY_TYPE_KEY"
private const val GROUP_BY_FAVORITES_KEY = "GROUP_BY_FAVORITES_KEY"

class SharedPrefsSortingAndGroupingRepository(private val preferences: SharedPreferences) :
    SortingAndGroupingRepository {

    override fun save(
        currentServerUrl: String,
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    ) {
        preferences.edit().putBoolean(SORT_BY_NAME_KEY + currentServerUrl, isSortByName).apply()
        preferences.edit().putBoolean(UNREAD_ON_TOP_KEY + currentServerUrl, isUnreadOnTop).apply()
        preferences.edit().putBoolean(GROUP_BY_TYPE_KEY + currentServerUrl, isGroupByType).apply()
        preferences.edit().putBoolean(GROUP_BY_FAVORITES_KEY + currentServerUrl, isGroupByFavorites)
            .apply()
    }

    override fun getSortByName(currentServerUrl: String): Boolean =
        preferences.getBoolean(SORT_BY_NAME_KEY + currentServerUrl, false)

    override fun getUnreadOnTop(currentServerUrl: String): Boolean =
        preferences.getBoolean(UNREAD_ON_TOP_KEY + currentServerUrl, false)

    override fun getGroupByType(currentServerUrl: String): Boolean =
        preferences.getBoolean(GROUP_BY_TYPE_KEY + currentServerUrl, false)

    override fun getGroupByFavorites(currentServerUrl: String): Boolean =
        preferences.getBoolean(GROUP_BY_FAVORITES_KEY + currentServerUrl, false)
}