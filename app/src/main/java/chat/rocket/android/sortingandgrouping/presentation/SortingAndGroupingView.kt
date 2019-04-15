package chat.rocket.android.sortingandgrouping.presentation

interface SortingAndGroupingView {

    /**
     * Shows the sorting and grouping preferences for the current logged in server.
     *
     * @param isSortByName True if sorting by name, false otherwise.
     * @param isUnreadOnTop True if grouping by unread on top, false otherwise.
     * @param isGroupByType True if grouping by type , false otherwise.
     * @param isGroupByFavorites True if grouping by favorites, false otherwise.
     */
    fun showSortingAndGroupingPreferences(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    )
}