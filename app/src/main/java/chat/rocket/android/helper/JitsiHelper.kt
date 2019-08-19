package chat.rocket.android.helper

object JitsiHelper {

    /**
     * Returns the Jitsi video conferencing server URL.
     *
     * @param isSecureProtocol True if using SSL, false otherwise - from the public settings.
     * @param domain The Jitsi domain - from public settings.
     */
    fun getJitsiServerUrl(
        isSecureProtocol: Boolean,
        domain: String?
    ): String = getJitsiProtocol(isSecureProtocol) + domain

    /**
     * Returns the Jitsi video conferencing room.
     *
     * @param prefix The Jitsi prefix - from public settings.
     * @param uniqueIdentifier The server unique identifier - from public settings.
     * @param chatRoomId The ChatRoom ID where the video conferencing was started.
     */
    fun getJitsiRoom(
        prefix: String?,
        uniqueIdentifier: String?,
        chatRoomId: String?
    ): String = prefix + uniqueIdentifier + chatRoomId

    private fun getJitsiProtocol(isSecureProtocol: Boolean) =
        if (isSecureProtocol) "https://" else "http://"
}