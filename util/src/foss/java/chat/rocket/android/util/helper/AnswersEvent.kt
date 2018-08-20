package chat.rocket.android.util.helper

object AnswersEvent {
    const val LOGIN_OR_SIGN_UP_BY_USER_AND_PASSWORD = "USER-AND-PASSWORD"
    const val LOGIN_BY_CAS = "CAS"
    const val LOGIN_BY_SAML = "SAML"
    const val LOGIN_OR_SIGN_UP_BY_OAUTH = "OAUTH"
    const val LOGIN_BY_DEEP_LINK = "DEEP-LINK"

    /**
     * Logs the Log In event.
     *
     * @param loginMethod The method that the user used to log in.
     * @param loginSucceeded True if the user successful logged in, false otherwise.
     */
    fun logLogin(loginMethod: String, loginSucceeded: Boolean) {
        // Do absolutely nothing
    }

    /**
     * Logs the Sign Up event.
     *
     * @param signUpMethod The method that the user used to sign up.
     * @param signUpSucceeded True if the user successful signed up, false otherwise.
     */
    fun logSignUp(signUpMethod: String, signUpSucceeded: Boolean) {
        // Do absolutely nothing
    }

    /**
     * Logs the screen view custom event.
     *
     * @param screenName The name of the screen to log.
     */
    fun logScreenView(screenName: String) {
        // Do absolutely nothing
    }

    /**
     * Logs the message sent custom event.
     *
     * @param roomType The room type to log.
     * @param serverUrl The server URL to log.
     */
    fun logMessageSent(roomType: String, serverUrl: String) {
        // Do absolutely nothing
    }

    /**
     * Logs the media upload custom event.
     *
     * @param roomType The room type to log.
     * @param mimeType The mime type of the media to log.
     */
    fun logMediaUploaded(roomType: String, mimeType: String) {
        // Do absolutely nothing
    }

    /**
     * Logs the reaction custom event.
     *
     * @param roomType The room type to log.
     */
    fun logReaction(roomType: String) {
        // Do absolutely nothing
    }

    /**
     * Logs the server switch custom event.
     *
     * @param serverUrl The server URL to log.
     * @param serverCount The number of server(s) the use own.
     */
    fun logServerSwitch(serverUrl: String, serverCount: Int) {
        // Do absolutely nothing
    }
}