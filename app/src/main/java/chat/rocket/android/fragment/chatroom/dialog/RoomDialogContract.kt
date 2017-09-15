package chat.rocket.android.fragment.chatroom.dialog

import chat.rocket.core.models.Message

interface RoomDialogContract {

    interface View {
        fun showPinnedMessages(dataSet: ArrayList<Message>)
        fun showFavoriteMessages()
        fun showFileList(dataSet: ArrayList<String>)
        fun showMemberList(dataSet: ArrayList<String>)
        fun showMessage(message: String)
    }

    interface Presenter {
        fun getDataSet(roomId: String,
                       roomType: String,
                       hostname: String,
                       token: String,
                       userId: String,
                       action: Int)
    }
}