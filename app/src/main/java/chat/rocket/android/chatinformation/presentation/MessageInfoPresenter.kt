package chat.rocket.android.chatinformation.presentation

import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.getMessageReadReceipts
import timber.log.Timber
import javax.inject.Inject

class MessageInfoPresenter @Inject constructor(
    private val view: MessageInfoView,
    private val strategy: CancelStrategy,
    private val mapper: UiModelMapper,
    serverInteractor: GetCurrentServerInteractor,
    factory: ConnectionManagerFactory
) {

    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client

    fun loadReadReceipts(messageId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                val readReceipts = retryIO(description = "getMessageReadReceipts") {
                    client.getMessageReadReceipts(messageId = messageId).result
                }
                view.showReadReceipts(mapper.map(readReceipts))
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                view.showGenericErrorMessage()
            } finally {
                view.hideLoading()
            }
        }
    }
}
