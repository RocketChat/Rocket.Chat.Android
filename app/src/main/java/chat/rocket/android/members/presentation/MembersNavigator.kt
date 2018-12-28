package chat.rocket.android.members.presentation

import chat.rocket.android.chatdetails.ui.ChatDetailsActivity
import chat.rocket.android.userdetails.ui.userDetailsIntent

class MembersNavigator(internal val activity: ChatDetailsActivity) {

    fun toMemberDetails(userId: String) {
        activity.apply {
            startActivity(this.userDetailsIntent(userId, ""))
        }
    }
}
