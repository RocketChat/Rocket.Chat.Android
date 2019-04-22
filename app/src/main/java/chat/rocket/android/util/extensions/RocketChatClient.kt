package chat.rocket.android.util.extensions

import chat.rocket.android.db.model.MessageEntity
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.core.internal.rest.registerPushToken
import chat.rocket.core.model.Message
import chat.rocket.core.model.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun RocketChatClientFactory.registerPushToken(
    token: String,
    accounts: List<Account>
) {
    withContext(Dispatchers.IO) {
        accounts.forEach { account ->
            try {
                retryIO(description = "register push token: ${account.serverUrl}") {
                    create(account.serverUrl).registerPushToken(token)
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error registering Push token for ${account.serverUrl}")
                ex.printStackTrace()
            }
        }
    }
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        roomId = roomId,
        message = message,
        timestamp = timestamp,
        senderId = sender?.id,
        updatedAt = updatedAt,
        editedAt = editedAt,
        editedBy = editedBy?.id,
        senderAlias = senderAlias,
        avatar = avatar,
        type = type.asString(),
        groupable = groupable,
        parseUrls = parseUrls,
        pinned = pinned,
        role = role,
        synced = synced
    )
}