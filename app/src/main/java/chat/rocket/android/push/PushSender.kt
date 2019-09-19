package chat.rocket.android.push

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class PushSender constructor(
    @Json(name = "_id") val id: String,
    val username: String?,
    val name: String?
)