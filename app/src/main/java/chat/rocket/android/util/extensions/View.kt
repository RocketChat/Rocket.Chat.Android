package chat.rocket.android.util.extensions

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import android.view.View
import chat.rocket.android.R

fun View.openTabbedUrl(url: Uri) {
    with(this) {
        val tabsbuilder = CustomTabsIntent.Builder()
        tabsbuilder.setToolbarColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
        val customTabsIntent = tabsbuilder.build()
        customTabsIntent.launchUrl(context, url)
    }
}