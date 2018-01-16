package chat.rocket.android.authentication.domain

/**
 * Created by luciofm on 1/15/18.
 */

interface CurrentTokenRepository {
    fun get(server: String): String

    fun save(server:String)
}
