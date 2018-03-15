package chat.rocket.android.profile.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.setAvatar
import chat.rocket.core.internal.rest.updateProfile
import javax.inject.Inject

class ProfilePresenter @Inject constructor(private val view: ProfileView,
                                           private val strategy: CancelStrategy,
                                           serverInteractor: GetCurrentServerInteractor,
                                           factory: RocketChatClientFactory) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private lateinit var myselfId: String

    fun loadUserProfile() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val myself = client.me()
                myselfId = myself.id
                val avatarUrl = UrlHelper.getAvatarUrl(serverUrl, myself.username!!)
                view.showProfile(
                        avatarUrl,
                        myself.name ?: "",
                        myself.username ?: "",
                        myself.emails?.get(0)?.address!!
                )
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

    fun updateUserProfile(email: String, name: String, username: String, avatarUrl: String = "") {
        launchUI(strategy) {
            view.showLoading()
            try {
                if(avatarUrl!="") {
                    client.setAvatar(avatarUrl)
                }
                val user = client.updateProfile(myselfId, email, name, username)
                view.showProfileUpdateSuccessfullyMessage()
                loadUserProfile()
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
}