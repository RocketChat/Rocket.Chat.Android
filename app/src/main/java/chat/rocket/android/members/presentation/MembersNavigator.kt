package chat.rocket.android.members.presentation

import chat.rocket.android.R
import chat.rocket.android.chatdetails.ui.ChatDetailsActivity
import chat.rocket.android.userdetails.ui.TAG_USER_DETAILS_FRAGMENT
import chat.rocket.android.util.extensions.addFragmentBackStack

class MembersNavigator(internal val activity: ChatDetailsActivity) {

    fun toMemberDetails(userId: String) {
        // TODO
//        activity.addFragmentBackStack(TAG_USER_DETAILS_FRAGMENT, R.id.fragment_container) {
//            chat.rocket.android.userdetails.ui.newInstance(userId)
//        }
    }
}
