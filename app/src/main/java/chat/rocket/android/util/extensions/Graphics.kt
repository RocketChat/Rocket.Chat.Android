import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur


fun Bitmap.blurred(context: Context, intensity: Float = 25f): Bitmap {
    if (intensity <= 0 || intensity > 25) {
        throw IllegalStateException("Intensity out of range (0 < intensity <= 25).")
    }
    val bitmap = copy(config, true)

    val rs = RenderScript.create(context)
    val input = Allocation.createFromBitmap(rs, this, Allocation.MipmapControl.MIPMAP_NONE,
        Allocation.USAGE_SCRIPT)
    val output = Allocation.createTyped(rs, input.type)
    val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
    script.setRadius(intensity)
    script.setInput(input)
    script.forEach(output)
    output.copyTo(bitmap)
    return bitmap
}
