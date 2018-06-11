package chat.rocket.android.createchannel.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.members.viewmodel.MemberViewModel

interface CreateChannelView : LoadingView, MessageView {

    /**
     * Shows the server's users suggestion (on the basis of the user typing - the query).
     *
     * @param dataSet The list of server's users to show.
     */
    fun showUserSuggestion(dataSet: List<MemberViewModel>)

    /**
     * Shows the navigation drawer with the chat item checked before showing the chat list.
     * This function is invoked after successfully created the channel.
     */
    fun prepareToShowChatList()

    /**
     * Shows a message that a channel was successfully created.
     */
    fun showChannelCreatedSuccessfullyMessage()

    /**
     * Enables the user input.
     */
    fun enableUserInput()

    /**
     * Disables the user input.
     */
    fun disableUserInput()
}