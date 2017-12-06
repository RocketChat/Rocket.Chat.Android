package chat.rocket.android.app.utils

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.Nullable
import android.support.v4.graphics.ColorUtils
import com.facebook.common.internal.ByteStreams
import com.facebook.imageformat.ImageFormat
import com.facebook.imageformat.ImageFormatCheckerUtils
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.decoder.ImageDecoder
import com.facebook.imagepipeline.drawable.DrawableFactory
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.image.QualityInfo
import java.io.IOException

/**
 * Simple decoder that can decode color images that have the following format: <color>#FF5722</color>.
 *
 * @see {https://github.com/facebook/fresco/blob/master/samples/showcase/src/main/java/com/facebook/fresco/samples/showcase/imageformat/color/ColorImageExample.java}
 */
object ColorImage {
    // Custom ImageFormat for color images.
    private val imageFormatColor = ImageFormat("IMAGE_FORMAT_COLOR", "color")
    // XML color tag that our colors must start with.
    val colorTag = "<color>"

    /**
     * Creates a new image format checker for [ColorImage.imageFormatColor].
     *
     * @return the image format checker.
     */
    fun createFormatChecker(): ImageFormat.FormatChecker = ColorFormatChecker()

    /**
     * Creates a new decoder that can decode [ColorImage.imageFormatColor] images.
     *
     * @return the decoder.
     */
    fun createDecoder(): ImageDecoder = ColorDecoder()

    fun createDrawableFactory(): ColorDrawableFactory = ColorDrawableFactory()

    /**
     * Custom color format checker that verifies that the header of the file corresponds to our [ColorImage.colorTag].
     */
    class ColorFormatChecker : ImageFormat.FormatChecker {
        private val header = ImageFormatCheckerUtils.asciiBytes(colorTag)

        override fun getHeaderSize(): Int {
            return header.size
        }

        @Nullable override fun determineFormat(headerBytes: ByteArray, headerSize: Int): ImageFormat? {
            if (headerSize > getHeaderSize()) {
                if (ImageFormatCheckerUtils.startsWithPattern(headerBytes, header)) {
                    return imageFormatColor
                }
            }
            return null
        }
    }

    /**
     * Custom closeable color image that holds a single color int value.
     */
    class CloseableColorImage(@field:ColorInt @get:ColorInt val color: Int) : CloseableImage() {
        private var isClosed = false

        override fun close() {
            isClosed = true
        }

        override fun getSizeInBytes(): Int = 0

        override fun isClosed(): Boolean = isClosed

        override fun getWidth(): Int = 0

        override fun getHeight(): Int = 0
    }

    /**
     * Decodes a color XML tag: <color>#rrggbb</color>.
     */
    class ColorDecoder : ImageDecoder {

        @Nullable override fun decode(encodedImage: EncodedImage, length: Int, qualityInfo: QualityInfo, options: ImageDecodeOptions): CloseableImage? {
            try {
                // Read the file as a string
                val text = String(ByteStreams.toByteArray(encodedImage.inputStream))

                // Check if the string matches "<color>#"
                if (!text.startsWith(colorTag + "#")) {
                    return null
                }

                // Parse the int value between # and <
                val startIndex = colorTag.length + 1
                val endIndex = text.lastIndexOf('<')
                var color = Integer.parseInt(text.substring(startIndex, endIndex), 16)

                // Add the alpha component so that we actually see the color
                color = ColorUtils.setAlphaComponent(color, 255)

                // Return the CloseableImage
                return CloseableColorImage(color)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            // Return nothing if an error occurred
            return null
        }
    }

    /**
     * Color drawable factory that is able to render a [CloseableColorImage] by creating a new [ColorDrawable] for the given color.
     */
    class ColorDrawableFactory : DrawableFactory {

        override fun supportsImageType(image: CloseableImage): Boolean {
            // We can only handle CloseableColorImages.
            return image is CloseableColorImage
        }

        @Nullable override fun createDrawable(image: CloseableImage): Drawable? {
            // Just return a simple ColorDrawable with the given color value.
            return ColorDrawable((image as CloseableColorImage).color)
        }
    }
}