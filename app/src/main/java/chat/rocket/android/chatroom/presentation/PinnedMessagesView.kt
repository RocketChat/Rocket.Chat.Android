package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface PinnedMessagesView : MessageView, LoadingView {

    /**
     * Show list of pinned messages for the current room.
     *
     * @param pinnedMessages The list of pinned messages.
     */
    fun showPinnedMessages(pinnedMessages: List<BaseViewModel<*>>)

    /**
     * Invoked when pinned message is unpinned.
     *
     * @param size Gives the size of pinned messages.
     */
    fun onUnpinMessage(size: Int)
}