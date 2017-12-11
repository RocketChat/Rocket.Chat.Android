package chat.rocket.android.authentication.presentation

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.signup
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
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

    fun signup(email: String, name: String, username: String, password: String) {
        // TODO - validate input

        job = launch(UI) {
            view.showProgress()

            try {
                val user = client.signup(email, name, username, password)
                Timber.d("Created user: $user")

                val token = client.login(username, password)
                Timber.d("Logged in: $token")

                view.hideProgress()
                navigator.toChatList()
            } catch (ex: RocketChatException) {
                view.hideProgress()
                view.onSignupError(ex.message)
            }
        }
    }

    fun unbind() {
        job?.let {
            it.cancel()
        }.also { null }
    }
}