package chat.rocket.android.analytics

import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.analytics.event.SubscriptionTypeEvent
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.answers.LoginEvent
import com.crashlytics.android.answers.SignUpEvent

class AnswersAnalytics : Analytics {

    override fun logLogin(event: AuthenticationEvent, loginSucceeded: Boolean) =
        Answers.getInstance()
            .logLogin(
                LoginEvent()
                    .putMethod(event.methodName)
                    .putSuccess(loginSucceeded)
            )

    override fun logSignUp(event: AuthenticationEvent, signUpSucceeded: Boolean) =
        Answers.getInstance()
            .logSignUp(
                SignUpEvent()
                    .putMethod(event.methodName)
                    .putSuccess(signUpSucceeded)
            )


    override fun logScreenView(event: ScreenViewEvent) =
        Answers.getInstance()
            .logCustom(CustomEvent("screen_view").putCustomAttribute("screen", event.screenName))


    override fun logMessageSent(event: SubscriptionTypeEvent, serverUrl: String) =
        Answers.getInstance()
            .logCustom(
                CustomEvent("message_sent")
                    .putCustomAttribute("subscription_type", event.subscriptionTypeName)
                    .putCustomAttribute("server", serverUrl)
            )


    override fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) =
        Answers.getInstance()
            .logCustom(
                CustomEvent("media_upload")
                    .putCustomAttribute("subscription_type", event.subscriptionTypeName)
                    .putCustomAttribute("media_type", mimeType)
            )


    override fun logReaction(event: SubscriptionTypeEvent) =
        Answers.getInstance()
            .logCustom(
                CustomEvent("reaction")
                    .putCustomAttribute("subscription_type", event.subscriptionTypeName)
            )


    override fun logServerSwitch(serverUrl: String, serverCount: Int) =
        Answers.getInstance()
            .logCustom(
                CustomEvent("server_switch")
                    .putCustomAttribute("server_url", serverUrl)
                    .putCustomAttribute("server_count", serverCount)
            )

    override fun logOpenAdmin() = Answers.getInstance().logCustom(CustomEvent("open_admin"))
}
