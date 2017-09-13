package chat.rocket.android.fragment.chatroom.dialog

interface RoomDialogContract {

    interface View {
        fun showPinnedMessages()
        fun showFavoriteMessages()
        fun showFileList(dataSet: ArrayList<String>)
        fun showMemberList(dataSet: ArrayList<String>)
        fun showMessage(message: String)
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