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
    fun getAvatarUrl(serverUrl: String, avatarName: String): String = removeTrailingSlash(serverUrl) + "/avatar/" + avatarName

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