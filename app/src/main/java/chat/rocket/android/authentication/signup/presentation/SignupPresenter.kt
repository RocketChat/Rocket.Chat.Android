package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.signup
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator,
                                          private val okHttpClient: OkHttpClient,
                                          private val logger: PlatformLogger,
                                          private val repository: AuthTokenRepository) {

    val client: RocketChatClient = RocketChatClient.create {
        httpClient = okHttpClient
        restUrl = HttpUrl.parse(navigator.currentServer)!!
        websocketUrl = navigator.currentServer!!
        tokenRepository = repository
        platformLogger = logger
    }

    fun signup(email: String, name: String, username: String, password: String) {
        // TODO - validate input

        launchUI(strategy) {
            view.showLoading()

            try {
                val user = client.signup(email, name, username, password)
                Timber.d("Created user: $user")

                val token = client.login(username, password)
                Timber.d("Logged in: $token")

                navigator.toChatList()
            } catch (ex: RocketChatException) {
                view.onSignupError(ex.message)
            } finally {
                view.hideLoading()
            }
        }
    }
}