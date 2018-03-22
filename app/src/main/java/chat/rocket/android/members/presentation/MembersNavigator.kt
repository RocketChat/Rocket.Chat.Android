package chat.rocket.android.members.presentation

import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.member.ui.newInstance
import chat.rocket.common.model.UserStatus

class MembersNavigator(internal val activity: ChatRoomActivity) {

    fun toMemberDetails(avatarUri: String, realName: String, username: String, email: String, utcOffset: String, userStatus: String) {
        activity.apply {
            newInstance(avatarUri, realName, username, email, utcOffset, userStatus)
                .show(supportFragmentManager, "MemberBottomSheetFragment")
        }
    }
}
