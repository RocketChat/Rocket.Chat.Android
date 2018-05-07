package chat.rocket.android.authentication.infraestructure

import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.util.DataToDomain
import chat.rocket.common.model.Token

object TokenMapper : DataToDomain<Token, TokenModel> {
    override fun translate(data: Token): TokenModel {
        return TokenModel(data.userId, data.authToken)
    }
}