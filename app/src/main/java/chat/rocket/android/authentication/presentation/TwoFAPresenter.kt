package chat.rocket.android.authentication.presentation

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val navigator: AuthenticationNavigator,
                                         private val okHttpClient: OkHttpClient,
                                         private val logger: PlatformLogger,
                                         private val repository: AuthTokenRepository) {

    var job: Job? = null
    val client: RocketChatClient = RocketChatClient.create {
        httpClient = okHttpClient
        restUrl = HttpUrl.parse(navigator.currentServer)!!
        websocketUrl = navigator.currentServer!!
        tokenRepository = repository
        platformLogger = logger
    }

    fun authenticate(username: String, password: String, pin: String) {
        // TODO - validate input

        job = launch(UI) {
            view.showProgress()
            try {
                val token = client.login(username, password, pin)

                view.hideProgress()
                navigator.toChatList()
            } catch (ex: RocketChatException) {
                view.hideProgress()
                view.onLoginError(ex.message)
            }
        }

    }

    fun unbind() {
        job?.let {
            it.cancel()
        }.also { null }
    }

    fun signup() {
        navigator.toSignUp(navigator.currentServer!!)
    }
}