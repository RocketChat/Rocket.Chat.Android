package chat.rocket.android.authentication.server.presentation

import okhttp3.HttpUrl

interface VersionCheckView {

    /**
     * Alerts the user about the server version not meeting the recommended server version.
     */
    fun alertNotRecommendedVersion()

    /**
     * Block user to proceed and alert him due to server having an unsupported server version.
     */
    fun blockAndAlertNotRequiredVersion()

    /**
     * Alerts the user that an error has occurred while checking the server version
     * This is optional.
     */
    fun errorCheckingServerVersion() {}

    /**
     * Do some action if version is ok. This is optional.
     */
    fun versionOk() {}

    /**
     * Alters the user this protocol is invalid. This is optional.
     */
    fun errorInvalidProtocol() {}

    /**
     * Updates the server URL after a URL redirection
     */
    fun updateServerUrl(url: HttpUrl) {}
}