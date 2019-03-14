package chat.rocket.android.helper

import java.net.URL

object JitsiHelper {

    /**
     * Returns the [URL] for the Jitsi video conferencing.
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
    ): URL =
        URL(
            getJitsiProtocol(isSecureProtocol) +
                    domain +
                    "/" +
                    prefix +
                    uniqueIdentifier +
                    chatRoomId
        )

    private fun getJitsiProtocol(isSecureProtocol: Boolean) =
        if (isSecureProtocol) "https://" else "http://"
}