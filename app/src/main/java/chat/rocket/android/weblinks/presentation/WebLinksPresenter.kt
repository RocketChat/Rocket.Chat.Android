package chat.rocket.android.weblinks.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.room.weblink.WebLinkDao
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import io.reactivex.Observable
import kotlinx.coroutines.experimental.async
import timber.log.Timber
import javax.inject.Inject

class WebLinksPresenter @Inject constructor(private val view: WebLinksView,
                                            private val strategy: CancelStrategy,
                                            private val webLinksDao: WebLinkDao) {

    fun loadWebLinks() {
        async {
            Observable.just(webLinksDao.getWebLinks())
                    .subscribe({
                        launchUI(strategy) {
                            try {
                                if (!it.isEmpty())
                                    view.updateWebLinks(it)
                                else
                                    view.showNoWebLinksToDisplay()
                            } catch (e: RocketChatException) {
                                Timber.e(e)
                            }
                        }
                    })
        }
    }
}