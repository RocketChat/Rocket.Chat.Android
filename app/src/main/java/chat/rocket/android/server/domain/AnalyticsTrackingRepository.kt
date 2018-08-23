package chat.rocket.android.server.domain

interface AnalyticsTrackingRepository {
    fun save(isAnalyticsTrackingEnable: Boolean)
    fun get(): Boolean
}