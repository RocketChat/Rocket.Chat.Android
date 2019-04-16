package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatRoomsView : LoadingView, MessageView {

    /**
     * Setups the toolbar with the current logged in server name.
     *
     * @param serverName The current logged in server name to show on Toolbar.
     */
    fun setupToolbar(serverName: String)

    /**
     * Setups the sorting and grouping in the bases of the user preference for
     * the current logged in server.
     *
     * @param isSortByName True if sorting by name, false otherwise.
     * @param isUnreadOnTop True if grouping by unread on top, false otherwise.
     * @param isGroupByType True if grouping by type , false otherwise.
     * @param isGroupByFavorites True if grouping by favorites, false otherwise.
     */
    fun setupSortingAndGrouping(
        isSortByName: Boolean,
        isUnreadOnTop: Boolean,
        isGroupByType: Boolean,
        isGroupByFavorites: Boolean
    )
}