package chat.rocket.android.helper

object UrlHelper {

    /**
     * Returns an URI whit no scheme (HTTP or HTTPS)
     *
     * @param uri The URI.
     * @return The URI whit no scheme (HTTP or HTTPS)
     */
    fun removeUriScheme(uri: String) = uri.replace("http://", "").replace("https://", "")

    /**
     * Returns the hostname with the security protocol (scheme) HTTPS.
     *
     * @param hostname The hostname.
     * @return The hostname with the security protocol (scheme) HTTPS.
     */
    fun getSafeHostname(hostname: String): String =
        "https://" + hostname.replace("http://", "").replace("https://", "")

    /**
     * Returns an URL with no spaces and inverted slashes.
     *
     * @param url The URL.
     * @return The URL with no spaces and inverted slashes.
     */
    fun getUrl(url: String) =
            url.replace(" ", "%20").replace("\\", "")

    /**
     * Returns an URL for a file.
     *
     * @param path The path to the file.
     * @param userId The user ID.
     * @param token The token.
     * @return The URL for a file
     */
    fun getUrlForFile(path: String, userId: String, token: String): String =
            "https://" + removeUriScheme(getUrl(path)) + "?rc_uid=$userId" + "&rc_token=$token"
}