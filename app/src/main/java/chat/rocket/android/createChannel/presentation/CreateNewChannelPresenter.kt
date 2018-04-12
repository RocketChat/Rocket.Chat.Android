package chat.rocket.android.createChannel.presentation

import android.widget.Toast
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.createChannel
import chat.rocket.core.model.Room
import javax.inject.Inject


class CreateNewChannelPresenter @Inject constructor(private val view: CreateNewChannelView,
                                                    private val strategy: CancelStrategy,
                                                    private val serverInteractor: GetCurrentServerInteractor,
                                                    factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    fun createNewChannel(roomType: RoomType, channelName: String, usersList: List<String>, readOnly: Boolean) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val channelCreated:Room? = client.createChannel(roomType, channelName, usersList, readOnly)
                view.showChannelCreatedSuccessfullyMessage()
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