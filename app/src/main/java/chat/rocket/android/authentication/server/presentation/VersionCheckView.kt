package chat.rocket.android.authentication.server.presentation

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
     * Do some action if version is ok. This is optional.
     */
    fun versionOk() {}
}