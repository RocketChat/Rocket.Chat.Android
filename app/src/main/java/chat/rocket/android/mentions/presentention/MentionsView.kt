package chat.rocket.android.mentions.presentention

import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface MentionsView : MessageView, LoadingView {

    /**
     * Shows the list of mentions for the current room.
     *
     * @param mentions The list of mentions.
     */
    fun showMentions(mentions: List<BaseUiModel<*>>)
}