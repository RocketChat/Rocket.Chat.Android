package chat.rocket.android.server.domain

import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.core.internal.rest.permissions
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * This class reloads the current logged server permission whenever its used.
 */
class RefreshPermissionsInteractor @Inject constructor(
    private val factory: RocketChatClientFactory,
    private val repository: PermissionsRepository
) {

    fun refreshAsync(server: String) {
        launch(CommonPool) {
            try {
                factory.create(server).let { client ->
                    val permissions = retryIO(
                        description = "permissions",
                        times = 5,
                        maxDelay = 5000,
                        initialDelay = 300
                    ) {
                        client.permissions()
                    }
                    repository.save(server, permissions)
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Error refreshing permissions for: $server")
            }
        }
    }
}