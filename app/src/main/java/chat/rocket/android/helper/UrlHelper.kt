package chat.rocket.android.helper

import android.util.Patterns

object UrlHelper {

    /**
     * Returns the avatar URL.
     *
     * @param serverUrl The server URL.
     * @param avatarName The avatar name.
     * @return The avatar URL.
     */
    fun getAvatarUrl(serverUrl: String, avatarName: String, format: String = "jpeg"): String =
        removeTrailingSlash(serverUrl) + "/avatar/" + removeTrailingSlash(avatarName) + "?format=$format"

    /**
     * Returns the server logo URL.
     *
     * @param serverUrl The server URL.
     * @param favicon The faviconLarge from the server settings.
     * @return The server logo URL.
     */
    fun getServerLogoUrl(serverUrl: String, favicon: String): String =
        removeTrailingSlash(serverUrl) + "/$favicon"

    /**
     * Returns the CAS URL.
     *
     * @param casLoginUrl The CAS login URL from the server settings.
     * @param serverUrl The server URL.
     * @param token The token to be send to the CAS server.
     * @return The avatar URL.
     */
    fun getCasUrl(casLoginUrl: String, serverUrl: String, token: String): String =
        removeTrailingSlash(casLoginUrl) + "?service=" + removeTrailingSlash(serverUrl) + "/_cas/" + token

    /**
     * Returns the server's Terms of Service URL.
     *
     * @param serverUrl The server URL.
     * @return The server's Terms of Service URL.
     */
    fun getTermsOfServiceUrl(serverUrl: String) = removeTrailingSlash(serverUrl) + "/terms-of-service"

    /**
     * Returns the server's Privacy Policy URL.
     *
     * @param serverUrl The server URL.
     * @return The server's Privacy Policy URL.
     */
    fun getPrivacyPolicyUrl(serverUrl: String) = removeTrailingSlash(serverUrl) + "/privacy-policy"

    /**
     * Returns an URL without trailing slash.
     *
     * @param serverUrl The URL to remove the trailing slash (if exists).
     * @return An URL without trailing slash.
     */
    fun removeTrailingSlash(serverUrl: String): String {
        return if (serverUrl[serverUrl.length - 1] == '/') {
            serverUrl.replace("/+$", "")
        } else {
            serverUrl
        }
    }

    /**
     * Checks if the given URL is valid or not.
     * @param url The url to check its valid.
     * @return True if url is valid, false otherwise.
     */
    fun isValidUrl(url: String): Boolean = Patterns.WEB_URL.matcher(url).matches()
}