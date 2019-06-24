package chat.rocket.android.util.extensions

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import android.view.View
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.thememanager.util.ThemeUtil
import timber.log.Timber

fun View.openTabbedUrl(url: String?) {
    if (url == null) return
    with(this) {
        val uri = url.ensureScheme()
        val tabsbuilder = CustomTabsIntent.Builder()
        tabsbuilder.setToolbarColor(ThemeUtil.getThemeColor(R.attr.colorPrimary))
        val customTabsIntent = tabsbuilder.build()
        try {
            customTabsIntent.launchUrl(context, uri)
        } catch (ex: Exception) {
            Timber.d(ex, "Unable to launch URL")
        }
    }
}

private fun String.ensureScheme(): Uri? {
    // check if the URL starts with a http(s) scheme
    val url = if (!this.matches(Regex("^([h|H][t|T][t|T][p|P]).*"))) {
        "http://$this"
    } else {
        this
    }

    return Uri.parse(url.lowercaseUrl())
}

inline var List<View>.isVisible: Boolean
    get() {
        var visible = true
        forEach { view ->
            if (!view.isVisible) {
                visible = false
            }
        }
        return visible
    }

    set(value) {
        forEach { view ->
            view.isVisible = value
        }
    }

fun List<View>.setOnClickListener(listener: (v: View) -> Unit) {
    forEach { view ->
        view.setOnClickListener(listener)
    }
}