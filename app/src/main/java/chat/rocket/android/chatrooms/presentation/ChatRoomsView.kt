package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.server.domain.model.Account

interface ChatRoomsView : LoadingView, MessageView {

    /**
     * Setups the toolbar with the current logged in server name.
     *
     * @param serverName The current logged in server name to show on Toolbar.
     */
    fun setupToolbar(serverName: String)

    fun showLoadingRoom(name: CharSequence)

    fun hideLoadingRoom()
}