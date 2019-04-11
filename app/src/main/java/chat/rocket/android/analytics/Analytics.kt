package chat.rocket.android.analytics

import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.analytics.event.SubscriptionTypeEvent

interface Analytics {

    /**
     * Logs the login event.
     *
     * @param event The [AuthenticationEvent] used to log in.
     * @param loginSucceeded True if successful logged in, false otherwise.
     */
    fun logLogin(event: AuthenticationEvent, loginSucceeded: Boolean) {}

    /**
     * Logs the sign up event.
     *
     * @param event The [AuthenticationEvent] used to sign up.
     * @param signUpSucceeded True if successful signed up, false otherwise.
     */
    fun logSignUp(event: AuthenticationEvent, signUpSucceeded: Boolean) {}

    /**
     * Logs the screen view event.
     *
     * @param event The [ScreenViewEvent] to log.
     */
    fun logScreenView(event: ScreenViewEvent) {}

    /**
     * Logs the message sent event.
     *
     * @param event The [SubscriptionTypeEvent] to log.
     * @param serverUrl The server URL to log.
     */
    fun logMessageSent(event: SubscriptionTypeEvent, serverUrl: String) {}

    /**
     * Logs the media upload event.
     *
     * @param event The [SubscriptionTypeEvent] to log.
     * @param mimeType The mime type of the media uploaded to log.
     */
    fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) {}

    /**
     * Logs the reaction event.
     *
     * @param event The [SubscriptionTypeEvent] to log.
     */
    fun logReaction(event: SubscriptionTypeEvent) {}

    /**
     * Logs the server switch event.
     *
     * @param serverUrl The server URL to log.
     * @param serverCount The number of server(s) the use own.
     */
    fun logServerSwitch(serverUrl: String, serverCount: Int) {}

    /**
     * Logs the admin opening event.
     */
    fun logOpenAdmin() {}

    /**
     * Logs the reset password event.
     *
     * @param resetPasswordSucceeded True if successful reset password, false otherwise.
     */
    fun logResetPassword(resetPasswordSucceeded: Boolean) {}

    /**
     * Logs the video conference event.
     *
     * @param event The [SubscriptionTypeEvent] to log.
     * @param serverUrl The server URL to log.
     */
    fun logVideoConference(event: SubscriptionTypeEvent, serverUrl: String) {}

    /**
     * Logs the add reaction message action.
     */
    fun logMessageActionAddReaction() {}

    /**
     * Logs the replay message action.
     */
    fun logMessageActionReply() {}

    /**
     * Logs the quote message action.
     */
    fun logMessageActionQuote() {}

    /**
     * Logs the permalink message action.
     */
    fun logMessageActionPermalink() {}

    /**
     * Logs the copy message action.
     */
    fun logMessageActionCopy() {}

    /**
     * Logs the edit message action.
     */
    fun logMessageActionEdit() {}

    /**
     * Logs the info message action.
     */
    fun logMessageActionInfo() {}

    /**
     * Logs the star message action.
     */
    fun logMessageActionStar() {}

    /**
     * Logs the pin message action.
     */
    fun logMessageActionPin() {}

    /**
     * Logs the report message action.
     */
    fun logMessageActionReport() {}

    /**
     * Logs the delete message action.
     */
    fun logMessageActionDelete() {}
}
