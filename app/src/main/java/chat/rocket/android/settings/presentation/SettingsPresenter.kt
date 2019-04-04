package chat.rocket.android.settings.presentation

import android.content.Context
import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.util.extension.launchUI
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    private val strategy: CancelStrategy,
    private val getAccountInteractor: GetAccountInteractor,
    serverInteractor: GetCurrentServerInteractor
) {
    @Inject
    lateinit var dynamicLinksManager : DynamicLinksForFirebase

    private val currentServer = serverInteractor.get()!!

    fun shareViaApp(context: Context){
        launchUI(strategy) {
            //get serverUrl and username
            val account = getAccountInteractor.get(currentServer)!!
            val userName = account.userName

            var deepLinkCallback = { returnedString: String? ->
                var link = if (returnedString != null) returnedString else context.getString(R.string.play_store_link)
                with(Intent(Intent.ACTION_SEND)) {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.msg_check_this_out))
                    putExtra(Intent.EXTRA_TEXT,link)
                    context.startActivity(Intent.createChooser(this, context.getString(R.string.msg_share_using)))
                }
            }
            dynamicLinksManager.createDynamicLink(userName, currentServer, deepLinkCallback)
        }
    }
}