package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import androidx.content.edit
import chat.rocket.android.server.domain.AccountsRepository
import chat.rocket.android.server.domain.model.Account
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class SharedPreferencesAccountsRepository(
    private val preferences: SharedPreferences,
    private val moshi: Moshi
) : AccountsRepository {

    override suspend fun save(newAccount: Account) {
        withContext(CommonPool) {
            val accounts = load()

            val newList = accounts.filter { account -> newAccount.serverUrl != account.serverUrl }
                    .toMutableList()
            newList.add(0, newAccount)
            save(newList)
        }
    }

    override suspend fun load(): List<Account> = withContext(CommonPool) {
        val json = preferences.getString(ACCOUNTS_KEY, "[]")
        val type = Types.newParameterizedType(List::class.java, Account::class.java)
        val adapter = moshi.adapter<List<Account>>(type)

        adapter.fromJson(json) ?: emptyList()
    }

    override suspend fun remove(serverUrl: String) {
        withContext(CommonPool) {
            val accounts = load()

            val newList = accounts.filter { account -> serverUrl != account.serverUrl }
                    .toMutableList()
            save(newList)
        }
    }

    private fun save(accounts: List<Account>) {
        val type = Types.newParameterizedType(List::class.java, Account::class.java)
        val adapter = moshi.adapter<List<Account>>(type)
        preferences.edit {
            putString(ACCOUNTS_KEY, adapter.toJson(accounts))
        }
    }
}

private const val ACCOUNTS_KEY = "ACCOUNTS_KEY"