package chat.rocket.android.settings.presentation

import chat.rocket.android.R
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.requestDataDownload
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class settingPresenter @Inject constructor(
        private val view: SettingsView,
        private val strategy: CancelStrategy,
        val userHelper: UserHelper,

        serverInteractor: GetCurrentServerInteractor,
        factory: RocketChatClientFactory


) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val user = userHelper.user()





    /**
     * Tells the status of downloaded data. Whether it is already requested , completed or user is requesting it now.
     *
     */
    fun downloadData() {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {

                    user?.id?.let { id ->
                        val response = retryIO { client.requestDataDownload(id) }

                        view.hideLoading()
                        if (response.requested) {
                            view.showMessage(R.string.msg_download_data_request)
                        } else if (response.status.equals("completed")) {
                            view.showMessage(R.string.msg_download_data_request_completed)
                        } else
                            view.showMessage(R.string.msg_download_data_request_already_exist)
                    }
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

}
