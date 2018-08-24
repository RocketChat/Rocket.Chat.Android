package chat.rocket.android.util.helper.analytics

import chat.rocket.android.util.helper.analytics.event.AuthenticationEvent
import chat.rocket.android.util.helper.analytics.event.ScreenViewEvent
import chat.rocket.android.util.helper.analytics.event.SubscriptionTypeEvent

object AnalyticsManager : Analytics {

    override fun logLogin(event: AuthenticationEvent, loginSucceeded: Boolean) {
        // Do absolutely nothing
    }

    override fun logSignUp(event: AuthenticationEvent, signUpSucceeded: Boolean) {
        // Do absolutely nothing
    }

    override fun logScreenView(event: ScreenViewEvent) {
        // Do absolutely nothing
    }

    override fun logMessageSent(event: SubscriptionTypeEvent, serverUrl: String) {
        // Do absolutely nothing
    }

    override fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) {
        // Do absolutely nothing
    }

    override fun logReaction(event: SubscriptionTypeEvent) {
        // Do absolutely nothing
    }

    override fun logServerSwitch(serverUrl: String, serverCount: Int) {
        // Do absolutely nothing
    }
}
