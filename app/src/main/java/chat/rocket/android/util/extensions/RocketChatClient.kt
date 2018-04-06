package chat.rocket.android.util.extensions

import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.registerPushToken
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

suspend fun RocketChatClient.registerPushToken(
    token: String,
    accounts: List<Account>,
    factory: RocketChatClientFactory
) {
    launch(CommonPool) {
        accounts.forEach { account ->
            try {
                retryIO(description = "register push token: ${account.serverUrl}") {
                    factory.create(account.serverUrl).registerPushToken(token)
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error registering Push token for ${account.serverUrl}")
                ex.printStackTrace()
            }
        }
    }
}