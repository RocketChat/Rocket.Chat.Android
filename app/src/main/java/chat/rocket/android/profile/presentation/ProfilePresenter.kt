package chat.rocket.android.profile.presentation

import android.graphics.Bitmap
import android.net.Uri
import chat.rocket.android.chatroom.domain.UriInteractor
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.compressImageAndGetByteArray
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.UserStatus
import chat.rocket.common.model.userStatusOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.setDefaultStatus
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.resetAvatar
import chat.rocket.core.internal.rest.setAvatar
import chat.rocket.core.internal.rest.updateProfile
import java.util.*
import javax.inject.Inject

class ProfilePresenter @Inject constructor(
    private val view: ProfileView,
    private val strategy: CancelStrategy,
    private val uriInteractor: UriInteractor,
    val userHelper: UserHelper,
    navigator: MainNavigator,
    serverInteractor: GetCurrentServerInteractor,
    factory: RocketChatClientFactory,
    removeAccountInteractor: RemoveAccountInteractor,
    tokenRepository: TokenRepository,
    dbManagerFactory: DatabaseManagerFactory,
    managerFactory: ConnectionManagerFactory
) : CheckServerPresenter(
    strategy = strategy,
    factory = factory,
    serverInteractor = serverInteractor,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    dbManagerFactory = dbManagerFactory,
    managerFactory = managerFactory,
    tokenView = view,
    navigator = navigator
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.get(serverUrl)
    private val user = userHelper.user()

    fun loadUserProfile() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val me = retryIO(description = "serverInfo", times = 5) {
                    client.me()
                }

                view.showProfile(
                    me.status.toString(),
                    serverUrl.avatarUrl(me.username ?: ""),
                    me.name ?: "",
                    me.username ?: "",
                    me.emails?.getOrNull(0)?.address ?: ""
                )
            } catch (exception: RocketChatException) {
                view.showMessage(exception)
            } finally {
                view.hideLoading()
            }
        }
    }

    fun updateUserProfile(email: String, name: String, username: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                user?.id?.let { id ->
                    retryIO {
                        client.updateProfile(
                            userId = id,
                            email = email,
                            name = name,
                            username = username
                        )
                    }
                    view.showProfileUpdateSuccessfullyMessage()
                    view.showProfile(
                        user.status.toString(),
                        serverUrl.avatarUrl(user.username ?: ""),
                        name,
                        username,
                        email
                    )
                }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        launchUI(strategy) {
            view.showLoading()
            try {
                retryIO {
                    client.setAvatar(
                        uriInteractor.getFileName(uri) ?: uri.toString(),
                        uriInteractor.getMimeType(uri)
                    ) {
                        uriInteractor.getInputStream(uri)
                    }
                }
                user?.username?.let { view.reloadUserAvatar(serverUrl.avatarUrl(it)) }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun preparePhotoAndUpdateAvatar(bitmap: Bitmap) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val byteArray = bitmap.compressImageAndGetByteArray("image/png")

                retryIO {
                    client.setAvatar(
                        UUID.randomUUID().toString() + ".png",
                        "image/png"
                    ) {
                        byteArray?.inputStream()
                    }
                }

                user?.username?.let { view.reloadUserAvatar(serverUrl.avatarUrl(it)) }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun resetAvatar() {
        launchUI(strategy) {
            view.showLoading()
            try {
                user?.id?.let { id ->
                    retryIO { client.resetAvatar(id) }
                }
                user?.username?.let { view.reloadUserAvatar(serverUrl.avatarUrl(it)) }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun updateStatus(status: UserStatus) {
        launchUI(strategy) {
            try {
                client.setDefaultStatus(status)
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }
}