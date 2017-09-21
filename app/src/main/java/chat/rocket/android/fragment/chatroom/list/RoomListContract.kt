package chat.rocket.android.fragment.chatroom.list

import chat.rocket.core.models.Message
import chat.rocket.core.models.User

interface RoomListContract {

    interface View {

        /**
         * Shows a pinned message list of a room.
         *
         * @param dataSet The pinned message data set to show.
         */
        fun showPinnedMessages(dataSet: ArrayList<Message>)

        /**
         * Shows a favorite message list of a room.
         *
         * @param dataSet The favorite message data set to show.
         */
        fun showFavoriteMessages(dataSet: ArrayList<Message>)

        /**
         * Shows a file list of a room.
         *
         * @param dataSet The file data set to show.
         */
        fun showFileList(dataSet: ArrayList<String>)

        /**
         * Shows a list of members of a room.
         *
         * @param dataSet The member data set to show.
         * @param total The total number of members.
         */
        fun showMemberList(dataSet: ArrayList<User>, total: String)

        /**
         * Shows a message (e.g. An error or successful message after a request).
         *
         * @param message The message to show.
         */
        fun showMessage(message: String)
    }

    interface Presenter {
        /**
         * Requests the pinned messages of a room.
         *
         * @param roomId The room ID to process the request.
         * @param roomType The room type to process the request.
         * @param hostname The server hostname to process the request.
         * @param token The token to process the request.
         * @param userId The user ID to process the request.
         * @param offset The offset to start to process the request.
         */
        fun requestPinnedMessages(roomId: String,
                                  roomType: String,
                                  hostname: String,
                                  token: String,
                                  userId: String,
                                  offset: String)

        /**
         * Requests the favorite messages of a room.
         *
         * @param roomId The room ID to process the request.
         * @param roomType The room type to process the request.
         * @param hostname The server hostname to process the request.
         * @param token The token to process the request.
         * @param userId The user ID to process the request.
         * @param offset The offset to start to process the request.
         */
        fun requestFavoriteMessages(roomId: String,
                                    roomType: String,
                                    hostname: String,
                                    token: String,
                                    userId: String,
                                    offset: String)

        /**
         * Requests the file list of a room.
         *
         * @param roomId The room ID to process the request.
         * @param roomType The room type to process the request.
         * @param hostname The server hostname to process the request.
         * @param token The token to process the request.
         * @param userId The user ID to process the request.
         * @param offset The offset to start to process the request.
         */
        fun requestFileList(roomId: String,
                            roomType: String,
                            hostname: String,
                            token: String,
                            userId: String,
                            offset: String)

        /**
         * Requests the member list of a room.
         *
         * @param roomId The room ID to process the request.
         * @param roomType The room type to process the request.
         * @param hostname The server hostname to process the request.
         * @param token The token to process the request.
         * @param userId The user ID to process the request.
         * @param offset The offset to start to process the request.
         */
        fun requestMemberList(roomId: String,
                              roomType: String,
                              hostname: String,
                              token: String,
                              userId: String,
                              offset: String)
    }
}