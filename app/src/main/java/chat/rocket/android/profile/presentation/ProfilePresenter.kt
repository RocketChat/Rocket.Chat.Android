package chat.rocket.android.profile.presentation

import android.util.Log
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
import java.io.File
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
                        myself.name!!,
                        myself.username!!,
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

    fun updateUserProfile(email: String, name: String, username: String, avatarImage: File) {
        launchUI(strategy) {
            view.showLoading()
            try {
                //TODO check for problems here
                Log.d("__VALUES__", "Name " + name + " email " + email + " username " + username)
                Log.d("__AVATAR__", avatarImage.absolutePath)
                val user = client.updateProfile(myselfId, email, name, username)
                val avatar = client.setAvatar(avatarImage, "image/jpeg")
                Log.d("STATUS", "profile_update" + user + "avatar_update" + avatar)
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