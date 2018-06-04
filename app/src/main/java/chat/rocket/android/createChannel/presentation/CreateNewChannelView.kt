package chat.rocket.android.createChannel.presentation

import chat.rocket.android.core.behaviours.LoadingView

interface CreateNewChannelView : LoadingView {
    /**
     * Show a message that a channel was successfully created
     */
    fun showChannelCreatedSuccessfullyMessage()

    /**
     * Show message and clear text in edit text
     *
     * @param resId Resource id of the message to be shown
     */
    fun showMessageAndClearText(resId: Int)

    /**
     * Show message and clear text in edit text
     *
     * @param message Toast message to be shown
     */

    fun showMessageAndClearText(message: String)

    /**
     * Show error message
     */
    fun showErrorMessage()

}