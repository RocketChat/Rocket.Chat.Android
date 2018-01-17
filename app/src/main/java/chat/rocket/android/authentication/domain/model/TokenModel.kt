package chat.rocket.android.authentication.domain.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TokenModel(val userId: String, val authToken: String)