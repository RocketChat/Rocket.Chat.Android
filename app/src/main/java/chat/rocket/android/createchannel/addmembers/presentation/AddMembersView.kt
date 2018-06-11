package chat.rocket.android.createchannel.addmembers.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.members.viewmodel.MemberViewModel

interface AddMembersView : LoadingView, MessageView {

    /**
     * Show the users of the server on the basis of query.
     *
     * @param dataSet The list of users to show.
     * @param total The number of users returned.
     */
    fun showUsers(dataSet: List<MemberViewModel>, total: Long)
}