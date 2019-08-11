package chat.rocket.android.thememanager.util

import android.content.Context
import android.content.res.Resources
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import androidx.annotation.AttrRes
import chat.rocket.android.R
import chat.rocket.android.thememanager.model.Theme

class ThemeUtil{

    companion object {
        @JvmStatic
        private lateinit var theme: Resources.Theme
        private lateinit var themeData: Theme
        private val typedValue: TypedValue = TypedValue()
        private const val resolveRefs: Boolean = true

        fun setTheme(theme: Resources.Theme, themeData: Theme) {
            this.theme = theme
            this.themeData = themeData
        }

        fun getThemeColor(@AttrRes attrColor: Int): Int {
            theme.resolveAttribute(attrColor, typedValue, resolveRefs)
            return typedValue.data
        }

        fun getThemeColorResource(@AttrRes attrColor: Int): Int {
            theme.resolveAttribute(attrColor, typedValue, resolveRefs)
            return typedValue.resourceId
        }

        fun getIsDark(context: Context): Boolean{
            theme.resolveAttribute(R.attr.colorBackgroundIsDark, typedValue, resolveRefs)
            return context.resources.getBoolean(typedValue.resourceId)
        }

        fun getTintedString(string: String, colorRef: Int): SpannableString{
            val spannableString = SpannableString(string)
            spannableString.setSpan( ForegroundColorSpan(getThemeColor(colorRef)), 0, spannableString.length, 0)
            return spannableString
        }
    }
}
