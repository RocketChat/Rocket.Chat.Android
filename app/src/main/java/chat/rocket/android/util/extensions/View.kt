package chat.rocket.android.util.extensions

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import android.view.View
import chat.rocket.android.R
import timber.log.Timber

fun View.openTabbedUrl(url: Uri) {
    with(this) {
        val tabsbuilder = CustomTabsIntent.Builder()
        tabsbuilder.setToolbarColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
        val customTabsIntent = tabsbuilder.build()
        try {
            customTabsIntent.launchUrl(context, url)
        } catch (ex: Exception) {
            Timber.d(ex, "Unable to launch URL")
        }
    }
}