package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import androidx.core.content.edit
import chat.rocket.android.server.domain.BasicAuthRepository
import chat.rocket.android.server.domain.model.BasicAuth
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private const val BASICAUTHS_KEY = "BASICAUTHS_KEY"

class SharedPrefsBasicAuthRepository(
    private val preferences: SharedPreferences,
    private val moshi: Moshi
) : BasicAuthRepository {

    override fun save(basicAuth: BasicAuth) {
        val newList = load().filter { auth -> auth.host != auth.host }.toMutableList()
        newList.add(0, basicAuth)
        save(newList)
    }

    override fun load(): List<BasicAuth> {
        val json = preferences.getString(BASICAUTHS_KEY, "[]")
        val type = Types.newParameterizedType(List::class.java, BasicAuth::class.java)
        val adapter = moshi.adapter<List<BasicAuth>>(type)

        return json?.let { adapter.fromJson(it) ?: emptyList() } ?: emptyList()
    }

    private fun save(basicAuths: List<BasicAuth>) {
        val type = Types.newParameterizedType(List::class.java, BasicAuth::class.java)
        val adapter = moshi.adapter<List<BasicAuth>>(type)
        preferences.edit {
            putString(BASICAUTHS_KEY, adapter.toJson(basicAuths))
        }
    }
}
