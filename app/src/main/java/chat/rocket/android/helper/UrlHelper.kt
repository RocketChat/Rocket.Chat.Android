package chat.rocket.android.helper

object UrlHelper {

    /**
     * Returns the avatar URL.
     *
     * @param serverUrl The serverUrl.
     * @param chatRoomName The chat room name.
     * @return The avatar URL.
     */
    fun getAvatarUrl(serverUrl: String, chatRoomName: String): String = serverUrl + "avatar/" + chatRoomName
}