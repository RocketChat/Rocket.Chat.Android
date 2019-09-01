package chat.rocket.android.push

import chat.rocket.common.model.RoomType
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class PushInfo @KotshiConstructor constructor(
    @Json(name = "host") val hostname: String,
    @Json(name = "rid") val roomId: String,
    val type: RoomType,
    val name: String?,
    val sender: PushSender?
) {
    val createdAt: Long
        get() = System.currentTimeMillis()
    val host by lazy {
        sanitizeUrl(hostname)
    }

    private fun sanitizeUrl(baseUrl: String): String {
        var url = baseUrl.trim()
        while (url.endsWith('/')) {
            url = url.dropLast(1)
        }

        return url
    }
}
