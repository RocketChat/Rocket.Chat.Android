package chat.rocket.android.customtab

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.res.ResourcesCompat
import chat.rocket.android.R
import chat.rocket.android.util.TimberLogger

object CustomTabHelper {
    fun openCustomTab(context: Context, uri: Uri) {
        val customTabIntentBuilder = CustomTabsIntent.Builder()
        customTabIntentBuilder.setToolbarColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))

        //Set action on clicking bookmark
        val actionLabel = context.resources.getString(R.string.customtab_bookmark_label)
        val icon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_bookmark)
        val pendingIntent = createPendingIntent(context, ActionBroadcastReceiver.ACTION_ACTION_BUTTON, uri)
        customTabIntentBuilder.setActionButton(icon, actionLabel, pendingIntent)

        val customTabIntent = customTabIntentBuilder.build()
        customTabIntent.launchUrl(context, uri)
    }

    private fun createPendingIntent(context: Context, actionSourceId: Int, uri: Uri): PendingIntent {
        TimberLogger.debug("weblink : pending intent : " + uri.toString())
        val actionIntent = Intent(context.applicationContext, ActionBroadcastReceiver::class.java)
        actionIntent.putExtra(ActionBroadcastReceiver.KEY_ACTION_SOURCE, actionSourceId)
        actionIntent.putExtra(ActionBroadcastReceiver.KEY_WEB_LINK_URI, uri)
        return PendingIntent.getBroadcast(context.applicationContext, actionSourceId, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}