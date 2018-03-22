package chat.rocket.android.customtab

import android.content.Context
import android.content.Intent
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.room.weblink.WebLinkDao
import chat.rocket.android.room.weblink.WebLinkEntity
import dagger.android.DaggerBroadcastReceiver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject


/**
 * A BroadcastReceiver that handles the Action Intent from the Custom Tab
 * when action button is clicked.
 */
class ActionBroadcastReceiver : DaggerBroadcastReceiver() {

    @Inject
    lateinit var webLinkDao: WebLinkDao

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val url = intent.dataString
        val actionId = intent.getIntExtra(KEY_ACTION_SOURCE, -1)
        if (url != null && actionId == ACTION_ACTION_BUTTON) {
            performBookmarkAction(context, url)
        }
    }

    private fun performBookmarkAction(context: Context, url: String) {
        launch {
            val webLink: WebLinkEntity? = webLinkDao.getWebLink(url)

            if (webLink != null) {
                webLinkDao.deleteWebLink(webLink)
                showToast(context.resources.getString(R.string.removed_bookmark))
            } else {
                webLinkDao.insertWebLink(WebLinkEntity(link = url))
                showToast(context.resources.getString(R.string.added_bookmark))
            }
        }
    }

    fun showToast(string: String) {
        Observable.just(string)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    Toast.makeText(RocketChatApplication.application.applicationContext, string, Toast.LENGTH_SHORT).show();
                })
    }

    companion object {
        const val KEY_ACTION_SOURCE = "action_source"
        const val ACTION_ACTION_BUTTON = 1
    }
}
