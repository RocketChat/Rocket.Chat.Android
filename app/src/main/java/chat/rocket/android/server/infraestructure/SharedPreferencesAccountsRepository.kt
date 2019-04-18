package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import androidx.core.content.edit
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.model.Account
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private const val ACCOUNTS_KEY = "ACCOUNTS_KEY"

class SharedPreferencesAccountsRepository(
    private val preferences: SharedPreferences,
    private val moshi: Moshi
) : AccountsRepository {

    override fun save(account: Account) {
        load().filter { it.serverUrl != account.serverUrl }
            .toMutableList()
            .apply {
                add(0, account)
                save(this)
            }
    }

    override fun load(): List<Account> {
        val json = preferences.getString(ACCOUNTS_KEY, "[]")
        val type = Types.newParameterizedType(List::class.java, Account::class.java)
        val adapter = moshi.adapter<List<Account>>(type)

        return json?.let { adapter.fromJson(it) } ?: emptyList()
    }

    override fun remove(serverUrl: String) {
        save(load().filter { account -> serverUrl != account.serverUrl }.toMutableList())
    }

    private fun save(accounts: List<Account>) {
        val type = Types.newParameterizedType(List::class.java, Account::class.java)
        val adapter = moshi.adapter<List<Account>>(type)
        preferences.edit { putString(ACCOUNTS_KEY, adapter.toJson(accounts)) }
    }
}