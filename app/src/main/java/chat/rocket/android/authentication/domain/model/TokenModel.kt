package chat.rocket.android.authentication.domain.model

//import chat.rocket.common.model.Token
import com.squareup.moshi.JsonReader
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TokenModel(val userId: String, val authToken: String)

fun TokenModel.toToken() = TokenModel (userId, authToken)
//fun TokenModel.toToken() = Token (userId, authToken)