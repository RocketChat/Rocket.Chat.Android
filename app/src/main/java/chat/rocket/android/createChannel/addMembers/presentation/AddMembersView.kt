package chat.rocket.android.createChannel.addMembers.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.members.viewmodel.MemberViewModel

interface AddMembersView : LoadingView, MessageView {
    /**
     * Show members on the basis of query
     * @param dataSet The list of members
     * @param total The number of members returned
     */
    fun showMembers(dataSet: List<MemberViewModel>, total: Long)
}