package chat.rocket.android.main.presentation

import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.main.viewmodel.NavHeaderViewModel

interface MainView : MessageView {

    fun setupNavHeader(navHeaderViewModel: NavHeaderViewModel)

    /**
     * User has successfully logged out from the current server.
     **/
    fun onLogout()
}