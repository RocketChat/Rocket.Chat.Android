package chat.rocket.android.helper

object UrlHelper {

    /**
     * Returns an URI whit no scheme (HTTP or HTTPS)
     *
     * @param uri The URI.
     * @return The URI whit no scheme (HTTP or HTTPS)
     */
    fun removeUriScheme(uri: String) =
            uri.replace("http://", "").replace("https://", "")

    /**
     * Returns the hostname with the security protocol (scheme) HTTPS.
     *
     * @param hostname The hostname.
     * @return The hostname with the security protocol (scheme) HTTPS.
     */
    fun getSafeHostname(hostname: String): String =
            "https://" + removeUriScheme(hostname)

    /**
     * Returns an URL with no spaces and inverted slashes.
     *
     * @param url The URL.
     * @return The URL with no spaces and inverted slashes.
     */
    fun getSafeUrl(url: String) =
            url.replace(" ", "%20").replace("\\", "")

    /**
     * Returns an attachment link.
     *
     * @param hostname The hostname.
     * @param fileId The file ID.
     * @param fileName The file name.
     * @return The attachment link.
     */
    fun getAttachmentLink(hostname: String, fileId: String, fileName: String): String =
            getSafeUrl(getSafeHostname(hostname) + "/file-upload/" + fileId + "/" + fileName)
}