package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.settings
import java.security.InvalidParameterException
import javax.inject.Inject

class ServerPresenter @Inject constructor(private val view: ServerView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator,
                                          private val serverInteractor: SaveCurrentServerInteractor,
                                          private val settingsInteractor: SaveSettingsInteractor,
                                          private val factory: RocketChatClientFactory) {

    private var settingsFilter = arrayOf(SITE_URL, SITE_NAME,
            FAVICON_512, USE_REALNAME, ALLOW_ROOM_NAME_SPECIAL_CHARS, FAVORITE_ROOMS,
            ACCOUNT_LOGIN_FORM, ACCOUNT_GOOGLE, ACCOUNT_FACEBOOK, ACCOUNT_GITHUB, ACCOUNT_GITLAB,
            ACCOUNT_LINKEDIN, ACCOUNT_METEOR, ACCOUNT_TWITTER, ACCOUNT_WORDPRESS, LDAP_ENABLE,
            ACCOUNT_REGISTRATION, STORAGE_TYPE, HIDE_USER_JOIN, HIDE_USER_LEAVE, HIDE_TYPE_AU,
            HIDE_MUTE_UNMUTE, HIDE_TYPE_RU, ACCOUNT_CUSTOM_FIELDS)

    fun connect(server: String) {
        var cli: RocketChatClient? = null
        try {
            cli = factory.create(server)
        } catch (ex: InvalidParameterException) {
            view.showMessage(ex.message!!)
        }

        cli?.let { client ->
            launchUI(strategy) {
                if (NetworkHelper.hasInternetAccess()) {
                    view.showLoading()

                    try {
                        val settings = client.settings(*settingsFilter)
                        settingsInteractor.save(server, settings)
                        serverInteractor.save(server)

                        navigator.toLogin()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        view.showMessage(ex.message!!)
                    } finally {
                        view.hideLoading()
                    }
                } else {
                    view.showNoInternetConnection()
                }
            }
        }
    }
}