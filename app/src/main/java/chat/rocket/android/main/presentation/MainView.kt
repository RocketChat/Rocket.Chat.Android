package chat.rocket.android.main.presentation

import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.presentation.TokenView

interface MainView : TokenView, MessageView {

    /**
     * Setups the toolbar with the current logged in server name.
     *
     * @param serverName The current logged in server name to show on Toolbar.
     */
    fun setupToolbar(serverName: String)

    /**
     * Setups the server list view with all server list.
     *
     * @param serverList The server list to show on server list.
     */
    fun setupServerListView(serverList: List<Account>)
}