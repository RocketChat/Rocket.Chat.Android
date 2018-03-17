package chat.rocket.android.customtab

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.util.TimberLogger
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
        val data = intent.dataString
        if (data != null) {
            val actionId = intent.getIntExtra(KEY_ACTION_SOURCE, -1)
            if (actionId == ACTION_ACTION_BUTTON) {
                val uri = intent.getParcelableExtra<Uri>(KEY_WEB_LINK_URI)
                TimberLogger.debug("weblink: " + uri.toString())

                launch {
                    val webLink = webLinkDao.getWebLink(uri.toString())

                    if (webLink != null) {
                        webLinkDao.deleteWebLink(webLink)
                        showToast("Removed Bookmark")
                    } else {
                        webLinkDao.insertWebLink(WebLinkEntity(link = uri.toString()))
                        showToast("Added Bookmark")
                    }
                }
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
        const val KEY_WEB_LINK_URI = "web_link_uri"
        const val ACTION_ACTION_BUTTON = 1
    }
}
