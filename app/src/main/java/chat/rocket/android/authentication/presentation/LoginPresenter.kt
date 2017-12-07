package chat.rocket.android.authentication.presentation

import chat.rocket.common.RocketChatException
import chat.rocket.common.model.Token
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.TokenRepository
import chat.rocket.core.internal.rest.login
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val navigator: AuthenticationNavigator,
                                         private val okHttpClient: OkHttpClient,
                                         private val logger: PlatformLogger) {

    var job: Job? = null
    val client: RocketChatClient = RocketChatClient.create {
        httpClient = okHttpClient
        restUrl = HttpUrl.parse(navigator.currentServer)!!
        websocketUrl = navigator.currentServer!!
        tokenRepository = SimpleTokenProvider()
        platformLogger = logger
    }


    fun authenticate(username: String, password: String) {
        // TODO - validate input

        job = launch(UI) {
            view.showProgress()
            try {
                val token = client.login(username, password)

                view.hideProgress()
                navigator.toChatRoom()
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
        navigator.toSignUp()
    }
}

class SimpleTokenProvider : TokenRepository {
    var savedToken: Token? = null
    override fun get(): Token? {
        return savedToken
    }

    override fun save(token: Token) {
        savedToken = token
    }
}