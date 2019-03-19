package chat.rocket.android.helper

object JitsiHelper {

    /**
     * Returns the for the Jitsi video conferencing URL.
     *
     * @param isSecureProtocol True if using SSL, false otherwise - from the public settings.
     * @param domain The Jitsi domain - from public settings.
     * @param prefix The Jitsi prefix - from public settings.
     * @param uniqueIdentifier The server unique identifier - from public settings.
     * @param chatRoomId The ChatRoom ID where the video conferencing was started.
     */
    fun getJitsiUrl(
        isSecureProtocol: Boolean,
        domain: String?,
        prefix: String?,
        uniqueIdentifier: String?,
        chatRoomId: String?
    ): String =
        getJitsiProtocol(isSecureProtocol) +
                domain +
                "/" +
                prefix +
                uniqueIdentifier +
                chatRoomId

    private fun getJitsiProtocol(isSecureProtocol: Boolean) =
        if (isSecureProtocol) "https://" else "http://"
}