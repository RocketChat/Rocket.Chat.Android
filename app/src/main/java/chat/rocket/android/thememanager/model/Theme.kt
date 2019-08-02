package chat.rocket.android.thememanager.model

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StyleableRes
import chat.rocket.android.R

data class Theme(val name: String, val colorArray: Int, val isDark: Boolean) {
    var customAccent = 0
    var customToolbar = 0
    var customBackground = 0
    var customName = ""
    var custom = false
    var colorPreviewBackground = 0
    var colorPreviewPrimary = 0
    var colorPreviewAccent = 0
    var colorPreviewDescriptiveText = 0
    var colorPreviewPrimaryText = 0
    @StyleableRes
    var index = 0

    override fun toString(): String {
        return name
    }

    fun setPreviewColors(context: Context) {
        val typedArray = context.resources.obtainTypedArray(colorArray)
        index = 0
        colorPreviewAccent = typedArray.getResourceId(index++, 0)
        if (customAccent != 0) {
            colorPreviewAccent = customAccent
        }
        colorPreviewPrimaryText = typedArray.getResourceId(index++, 0)
        colorPreviewPrimary = typedArray.getResourceId(index++, 0)
        if (customToolbar != 0) {
            colorPreviewPrimary = customToolbar
        }
        colorPreviewDescriptiveText = typedArray.getResourceId(index++, 0)
        colorPreviewBackground = typedArray.getResourceId(index++, 0)
        if (customBackground != 0) {
            colorPreviewBackground = customBackground
        }
        typedArray.recycle()
    }

    fun getCustomAccentStyle(resources: Resources, packageName: String): Int {
        var style = 0
        if (customAccent != 0) {
            val color = resources.getResourceEntryName(customAccent)
                style = resources.getIdentifier(color + "ColorAccent", "style", packageName)
        }
        return style
    }

    fun getCustomToolbarStyle(resources: Resources, packageName: String): Int {
        var style = 0
        if (customToolbar != 0) {
            val color = resources.getResourceEntryName(customToolbar)
                style = resources.getIdentifier(color + "ColorToolbar", "style", packageName)
        }
        return style
    }

    fun getCustomBackgroundStyle(resources: Resources, packageName: String): Int {
        var style = 0
        if (customBackground != 0) {
            val color = resources.getResourceEntryName(customBackground)
            style = resources.getIdentifier(color + name, "style", packageName)
        }
        return style
    }
}