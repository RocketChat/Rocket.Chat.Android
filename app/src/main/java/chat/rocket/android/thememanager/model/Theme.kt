package chat.rocket.android.thememanager.model

import android.content.Context
import androidx.annotation.StyleableRes

data class Theme(val name: String, val colorArray: Int) {
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
        index=0
        colorPreviewAccent = typedArray.getResourceId(index++, 0)
        colorPreviewPrimaryText = typedArray.getResourceId(index++, 0)
        colorPreviewPrimary = typedArray.getResourceId(index++, 0)
        colorPreviewDescriptiveText = typedArray.getResourceId(index++, 0)
        colorPreviewBackground = typedArray.getResourceId(index++, 0)
        typedArray.recycle()
    }
}