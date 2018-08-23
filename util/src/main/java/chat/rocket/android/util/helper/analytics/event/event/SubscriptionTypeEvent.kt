package chat.rocket.android.util.helper.analytics.event

sealed class SubscriptionTypeEvent(val subscriptionTypeName: String) {
    object DirectMessage : SubscriptionTypeEvent("Direct Message")
    object Channel : SubscriptionTypeEvent("Channel")
    object Group : SubscriptionTypeEvent("Group")
}