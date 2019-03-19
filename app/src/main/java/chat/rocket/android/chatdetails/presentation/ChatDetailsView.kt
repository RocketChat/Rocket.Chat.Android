package chat.rocket.android.chatdetails.presentation

import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatDetailsView: MessageView {

    /**
     * Shows the corresponding favorite icon for a favorite or non-favorite chat room.
     *
     * @param isFavorite True if a chat room is favorite, false otherwise.
     */
    fun showFavoriteIcon(isFavorite: Boolean)

    /**
     * Shows the details of a chat room.
     */
    fun displayDetails(room: ChatDetails)
}