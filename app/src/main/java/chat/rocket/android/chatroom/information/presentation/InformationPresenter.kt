package chat.rocket.android.chatroom.information.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.util.ifNull
import javax.inject.Inject

class InformationPresenter @Inject constructor(private val view: InformationView,
                                               private val chatRoomsInteractor: GetChatRoomsInteractor,
                                               private val serverInteractor: GetCurrentServerInteractor,
                                               private val strategy: CancelStrategy) {
    fun loadRoomInfo(chatRoomId: String) {
        view.showLoading()

        launchUI(strategy) {
            try {
                val serverUrl = serverInteractor.get()!!
                view.showRoomInfo(chatRoomsInteractor.getById(serverUrl, chatRoomId)!!)
            } catch (ex: Exception) {
                ex.printStackTrace()
                ex.message?.let {
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
