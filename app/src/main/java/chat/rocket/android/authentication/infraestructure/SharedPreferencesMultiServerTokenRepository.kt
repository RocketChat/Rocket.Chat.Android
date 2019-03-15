package chat.rocket.android.authentication.infraestructure

import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.dagger.scope.PerActivity
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.LocalRepository.Companion.TOKEN_KEY
import chat.rocket.android.server.domain.MultiServerTokenRepository
import com.squareup.moshi.Moshi

@PerActivity
class SharedPreferencesMultiServerTokenRepository(
    private val repository: LocalRepository,
    private val moshi: Moshi
) : MultiServerTokenRepository {

    override fun get(server: String): TokenModel? {
        val token = repository.get("$TOKEN_KEY$server")
        val adapter = moshi.adapter<TokenModel>(TokenModel::class.java)

        return token?.let { adapter.fromJson(it) }
    }

    override fun save(server: String, token: TokenModel) {
        val adapter = moshi.adapter<TokenModel>(TokenModel::class.java)

        repository.save("$TOKEN_KEY$server", adapter.toJson(token))
    }

    override fun clear(server: String) {
        repository.clear("$TOKEN_KEY$server")
    }
}