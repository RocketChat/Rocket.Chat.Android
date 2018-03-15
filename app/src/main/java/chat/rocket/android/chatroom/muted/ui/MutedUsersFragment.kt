package chat.rocket.android.chatroom.muted.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import chat.rocket.android.chatroom.muted.presentation.MutedUsersView

fun newInstance(chatRoomId: String): Fragment {
    return MutedUsersFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"

class MutedUsersFragment: Fragment(), MutedUsersView {
    override fun showLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMutedUsers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(resId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showGenericErrorMessage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
