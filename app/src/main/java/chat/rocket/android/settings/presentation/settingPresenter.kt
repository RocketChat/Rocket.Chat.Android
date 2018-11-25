package chat.rocket.android.settings.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.requestDataDownload
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class settingPresenter @Inject constructor(
    private val view: SettingsView,
    private val strategy: CancelStrategy,
    serverInteractor: GetCurrentServerInteractor,
    factory: RocketChatClientFactory


) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)





    fun downloadData() {
        launchUI(strategy) {
            //view.showLoading()
            try {
                withContext(DefaultDispatcher) {
                    // REMARK: Backend API is only working with a lowercase hash.
                    // https://github.com/RocketChat/Rocket.Chat/issues/12573

                    val me = retryIO("me") { client.me() }
                    val response = retryIO { client.requestDataDownload(me.id) }
                   // view.hideLoading()
                    if(response.requested){
                    view.showDownloadDialog("UserDataDownload_Requested_Text")}
                   else if(response.status.equals("completed")){
                        view.showDownloadDialog("UserDataDownload_CompletedRequestExisted_Text")
                    }else
                        view.showDownloadDialog("UserDataDownload_RequestExisted_Text")

                }
            } catch (exception: Exception) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
              //  view.hideLoading()
            }
        }
    }

}
