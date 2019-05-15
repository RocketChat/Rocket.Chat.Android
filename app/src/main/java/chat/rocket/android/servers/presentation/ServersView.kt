package chat.rocket.android.servers.presentation

import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.server.domain.model.Account

interface ServersView : MessageView {

    /**
     * Shows the server list.
     *
     * @param serverList The list of server to show.
     * @param currentServerUrl The current logged in server url.
     */
    fun showServerList(serverList: List<Account>, currentServerUrl: String)

    /**
     * Hides the servers view.
     */
    fun hideServerView()
}