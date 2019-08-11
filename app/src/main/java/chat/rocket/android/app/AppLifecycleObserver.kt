package chat.rocket.android.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.common.model.UserStatus
import chat.rocket.core.internal.realtime.setTemporaryStatus
import javax.inject.Inject
import javax.inject.Named

class AppLifecycleObserver @Inject constructor(
    @Named("currentServer") private val currentServer: String?,
    private val rocketChatClientFactory: RocketChatClientFactory,
    private val connectionManagerFactory: ConnectionManagerFactory
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        changeTemporaryStatus(UserStatus.Online())
        currentServer?.let {
            connectionManagerFactory.create(it)?.resetReconnectionTimer()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() =
        changeTemporaryStatus(UserStatus.Away())

    private fun changeTemporaryStatus(userStatus: UserStatus) = currentServer?.let {
        rocketChatClientFactory.get(it).setTemporaryStatus(userStatus)
    }

}