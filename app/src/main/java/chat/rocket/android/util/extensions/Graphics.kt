import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


fun Bitmap.blurred(context: Context, intensity: Float = 25f, newWidth: Int = -1, newHeight: Int = -1): Bitmap {
    if (intensity <= 0 || intensity > 25) {
        throw IllegalStateException("Intensity out of range (0 < intensity <= 25).")
    }
    val nonScaledBitmap = copy(config, true)
    val bitmap = if (newWidth > 0 && newHeight > 0) {
        val height = nonScaledBitmap.height
        val width = nonScaledBitmap.width
        val aspectRadio = width / height
        val adjustedWidth = newHeight * aspectRadio
        val adjustedHeight = newWidth * height / width
        val scaledBitmap = Bitmap.createScaledBitmap(nonScaledBitmap, adjustedWidth, adjustedHeight, false)
        Bitmap.createBitmap(scaledBitmap, 0, 0, adjustedWidth, newHeight)
    } else nonScaledBitmap
    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
        Allocation.USAGE_SCRIPT)
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(intensity)
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    return bitmap
}
