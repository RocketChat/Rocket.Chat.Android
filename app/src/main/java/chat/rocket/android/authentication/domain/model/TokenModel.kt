package chat.rocket.android.authentication.domain.model

import chat.rocket.common.model.Token
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TokenModel(val userId: String, val authToken: String)

fun TokenModel.toToken() = Token(userId, authToken)