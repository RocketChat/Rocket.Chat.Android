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
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.compressImageAndGetByteArray
import chat.rocket.android.util.extension.gethash
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extension.toHex
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.deleteOwnAccount
import chat.rocket.core.internal.rest.resetAvatar
import chat.rocket.core.internal.rest.setAvatar
import chat.rocket.core.internal.rest.updateProfile
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import java.util.*
import javax.inject.Inject

// WIDECHAT
import chat.rocket.core.internal.rest.getAccessToken
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.Protocol

class ProfilePresenter @Inject constructor(
    private val view: ProfileView,
    private val strategy: CancelStrategy,
    private val uriInteractor: UriInteractor,
    val userHelper: UserHelper,
    navigator: MainNavigator,
    serverInteractor: GetCurrentServerInteractor,
    refreshSettingsInteractor: RefreshSettingsInteractor,
    settingsInteractor: GetSettingsInteractor,
    factory: RocketChatClientFactory,
    removeAccountInteractor: RemoveAccountInteractor,
    tokenRepository: TokenRepository,
    dbManagerFactory: DatabaseManagerFactory,
    managerFactory: ConnectionManagerFactory
) : CheckServerPresenter(
    strategy = strategy,
    factory = factory,
    serverInteractor = serverInteractor,
    settingsInteractor = settingsInteractor,
    refreshSettingsInteractor = refreshSettingsInteractor,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    dbManagerFactory = dbManagerFactory,
    managerFactory = managerFactory,
    tokenView = view,
    navigator = navigator
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val user = userHelper.user()

    // WIDECHAT
    var currentAccessToken: String? = null
    private val ssoApiClient = OkHttpClient().newBuilder().protocols(Arrays.asList(Protocol.HTTP_1_1))

    fun loadUserProfile() {
        launchUI(strategy) {
            view.showLoading()
            try {
                view.showProfile(
                    serverUrl.avatarUrl(user?.username ?: ""),
                    user?.name ?: "",
                    user?.username ?: "",
                    user?.emails?.getOrNull(0)?.address ?: ""
                )
            } catch (exception: RocketChatException) {
                view.showMessage(exception)
            } finally {
                view.hideLoading()
            }
        }
    }

    // WIDECHAT - profile update with SSO
    fun setUpdateUrl(updatePath: String?, onClickCallback: (String?) -> Unit?) {
        launchUI(strategy) {
            try {
                withContext(DefaultDispatcher) {
                    setupConnectionInfo(serverUrl)
                    refreshServerAccounts()
                    checkEnabledAccounts(serverUrl)
                }
                retryIO { currentAccessToken = client.getAccessToken(customOauthServiceName.toString()) }
                onClickCallback("${customOauthHost}${updatePath}${currentAccessToken}")
            } catch (ex: Exception) {
                view.showMessage(ex)
            }
        }
    }

    fun updateUserProfile(email: String, name: String, username: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                user?.id?.let { id ->
                    retryIO { client.updateProfile(id, email, name, username) }
                    view.showProfileUpdateSuccessfullyMessage()
                    view.showProfile(
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
                user?.username?.let { view.reloadUserAvatar(it) }
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

                user?.username?.let { view.reloadUserAvatar(it) }
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
                user?.username?.let { view.reloadUserAvatar(it) }
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

    fun deleteAccount(password: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {
                    // REMARK: Backend API is only working with a lowercase hash.
                    // https://github.com/RocketChat/Rocket.Chat/issues/12573
                    retryIO { client.deleteOwnAccount(password.gethash().toHex().toLowerCase()) }
                    setupConnectionInfo(serverUrl)
                    logout(null)
                }
            } catch (exception: Exception) {
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

    // WIDECHAT
    fun deleteAccount(username: String, ssoDeleteCallback: () -> Unit?) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {
                    retryIO { client.deleteOwnAccount(username) }
                    ssoDeleteCallback()
                    setupConnectionInfo(serverUrl)
                    logout(null)
                }
            } catch (exception: Exception) {
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

    // TODO: Is it neccessary to move this into the Kotlin SDK?
    fun widechatDeleteSsoAccount() {
        val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8")
        val json = """{"profilemap":{"username":"userid"}}""".trimIndent()

        var request: Request = Request.Builder()
                .url("https://mysso.test.viasat.com/federation/custom/json/viasatconnect/user")
                .delete(RequestBody.create(MEDIA_TYPE_JSON, json))
                .addHeader("Authorization", "Bearer ${currentAccessToken}")
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build()

        // TODO: Implement validation check? What action upon failure?
        val response = ssoApiClient.build().newCall(request).execute()

    }
}
