package chat.rocket.android.push

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class PushSender @KotshiConstructor constructor(
    @Json(name = "_id") val id: String,
    val username: String?,
    val name: String?
)