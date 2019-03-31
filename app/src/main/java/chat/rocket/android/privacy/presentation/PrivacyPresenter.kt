package chat.rocket.android.privacy.presentation

import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.privacy.presentation.PrivacyView
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getDiscoverability
import chat.rocket.core.internal.rest.setDiscoverability
import timber.log.Timber
import javax.inject.Inject

class PrivacyPresenter @Inject constructor(
        private val view: PrivacyView,
        private val strategy: CancelStrategy,
        serverInteractor: GetCurrentServerInteractor,
        factory: RocketChatClientFactory
        ) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)

    fun setDiscoverability(discoverablity:String){
        launchUI(strategy) {
            try {
                retryIO { client.setDiscoverability(discoverablity) }
            } catch (exception: RocketChatException) {
                Timber.e(exception.message)
            } finally {
            }
        }
    }

    fun showDiscoverability() {
        launchUI(strategy) {
            try {
                val discoverability: String = client.getDiscoverability()
                view.showDiscoverability(discoverability)
            } catch (exception: RocketChatException) {
                Timber.e(exception.message)
            } finally {
            }
        }
    }
}