package chat.rocket.android.main.presentation

import chat.rocket.android.authentication.server.presentation.VersionCheckView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.main.uimodel.NavHeaderUiModel
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.presentation.TokenView
import chat.rocket.common.model.UserStatus

interface MainView : MessageView, VersionCheckView, TokenView {

    /**
     * Shows the current user status.
     *
     * @see [UserStatus]
     */
    fun showUserStatus(userStatus: UserStatus)

    /**
     * Setups the user account info (displayed in the nav. header)
     *
     * @param uiModel The [NavHeaderUiModel].
     */
    fun setupUserAccountInfo(uiModel: NavHeaderUiModel)

    /**
     * Setups the server account list.
     *
     * @param serverAccountList The list of server accounts.
     */
    fun setupServerAccountList(serverAccountList: List<Account>)

    fun closeServerSelection()

    fun showProgress()

    fun hideProgress()
}