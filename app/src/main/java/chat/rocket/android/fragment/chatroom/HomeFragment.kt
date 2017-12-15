package chat.rocket.android.fragment.chatroom

import chat.rocket.android.R

class HomeFragment : AbstractChatRoomFragment() {

    override fun getLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onSetupView() {
        setToolbarTitle(getText(R.string.home_fragment_title))
    }
}