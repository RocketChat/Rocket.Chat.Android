package chat.rocket.android.fragment.chatroom.dialog

interface RoomDialogContract {

    interface View {
        fun showPinnedMessages()
        fun showFavoriteMessages()
        fun showFileList()
        fun showMemberList()
    }

    interface Presenter {
        fun getDataSet(roomId: String,
                       roomName: String,
                       roomType: String,
                       hostname: String,
                       token: String,
                       userId: String,
                       action: Int)
    }
}