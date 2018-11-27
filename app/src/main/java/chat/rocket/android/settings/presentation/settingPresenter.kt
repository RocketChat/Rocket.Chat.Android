package chat.rocket.android.settings.presentation

import chat.rocket.android.R
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




// this method is written to execute download data call
    fun downloadData() {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {

                    val me = retryIO("me") { client.me() }
                    val response = retryIO { client.requestDataDownload(me.id) }
                   view.hideLoading()
                    if(response.requested){
                    view.showMessage(R.string.UserDataDownload_Requested_Text)}
                   else if(response.status.equals("completed")){
                        view.showMessage(R.string.UserDataDownload_CompletedRequestExisted_Text)
                    }else
                        view.showMessage(R.string.UserDataDownload_RequestExisted_Text)
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
