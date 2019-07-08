package chat.rocket.android.inviteusers.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.members.uimodel.MemberUiModel

interface InviteUsersView : LoadingView, MessageView {

    /**
     * Shows the server users suggestion (on the basis of the user typing - the query).
     *
     * @param dataSet The list of server's users to show.
     */
    fun showUserSuggestion(dataSet: List<MemberUiModel>)

    /**
     * Shows no servers users suggestion.
     */
    fun showNoUserSuggestion()

    /**
     * Shows the SuggestionView in progress.
     */
    fun showSuggestionViewInProgress()

    /**
     * Hides the progress shown in the SuggestionView.
     */
    fun hideSuggestionViewInProgress()

    /**
     * Take actions after users are successfully invited.
     */
    fun usersInvitedSuccessfully()

    /**
     * Enables the user input.
     */
    fun enableUserInput()

    /**
     * Disables the user input.
     */
    fun disableUserInput()
}