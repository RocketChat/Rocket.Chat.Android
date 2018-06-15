package chat.rocket.android.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.UserStatus
import chat.rocket.core.internal.realtime.setTemporaryStatus
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class AppLifecycleObserver @Inject constructor(
    private val serverInteractor: GetCurrentServerInteractor,
    private val factory: RocketChatClientFactory,
    private val getAccountInteractor: GetAccountInteractor
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        changeTemporaryStatus(UserStatus.Online())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        changeTemporaryStatus(UserStatus.Away())
    }

    private fun changeTemporaryStatus(userStatus: UserStatus) {
        launch {
            val currentServer = serverInteractor.get()
            val account = currentServer?.let { getAccountInteractor.get(currentServer) }
            val client = account?.let { factory.create(currentServer) }

            try {
                client?.setTemporaryStatus(userStatus)
            } catch (exception: RocketChatException) {
                Timber.e(exception)
            }
        }
    }
}