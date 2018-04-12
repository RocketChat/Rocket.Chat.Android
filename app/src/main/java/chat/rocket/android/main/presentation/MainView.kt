package chat.rocket.android.main.presentation

import chat.rocket.android.authentication.server.presentation.VersionCheckView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.main.viewmodel.NavHeaderViewModel
import chat.rocket.android.server.domain.model.Account
import chat.rocket.core.internal.realtime.UserStatus

interface MainView : MessageView, VersionCheckView {

    /**
     * Shows the current user status.
     *
     * @see [UserStatus]
     */
    fun showUserStatus(userStatus: UserStatus)

    /**
     * Setups the navigation header.
     *
     * @param viewModel The [NavHeaderViewModel].
     * @param accounts The list of accounts.
     */
    fun setupNavHeader(viewModel: NavHeaderViewModel, accounts: List<Account>)

    fun closeServerSelection()
}