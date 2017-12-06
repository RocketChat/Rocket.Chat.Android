package chat.rocket.android.app.utils

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.support.annotation.Nullable
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.facebook.imageformat.ImageFormat
import com.facebook.imageformat.ImageFormatCheckerUtils
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.decoder.ImageDecoder
import com.facebook.imagepipeline.drawable.DrawableFactory
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.EncodedImage
import com.facebook.imagepipeline.image.QualityInfo

/**
 * SVG example that defines all classes required to decode and render SVG images.
 *
 * @see {https://github.com/facebook/fresco/blob/master/samples/showcase/src/main/java/com/facebook/fresco/samples/showcase/imageformat/svg/SvgDecoderExample.java}
 */
object SvgDecoder {
    val svgFormat = ImageFormat("SVG_FORMAT", "svg")
    // We do not include the closing ">" since there can be additional information.
    private val headerTag = "<?xml"
    private val possibleHeaderTags = arrayOf(ImageFormatCheckerUtils.asciiBytes("<svg"))

    /**
     * Custom SVG format checker that verifies that the header of the file corresponds to our [SvgDecoder.headerTag] or [SvgDecoder.possibleHeaderTags].
     */
    class SvgFormatChecker : ImageFormat.FormatChecker {
        private val header = ImageFormatCheckerUtils.asciiBytes(headerTag)

        override fun getHeaderSize(): Int {
            return header.size
        }

        @Nullable override fun determineFormat(headerBytes: ByteArray, headerSize: Int): ImageFormat? {
            if (headerSize > getHeaderSize()) {
                if (ImageFormatCheckerUtils.startsWithPattern(headerBytes, header)) {
                    return svgFormat
                }

                if (possibleHeaderTags.any { ImageFormatCheckerUtils.startsWithPattern(headerBytes, it) && ImageFormatCheckerUtils.indexOfPattern(headerBytes, headerBytes.size, header, header.size) > -1 }) {
                    return svgFormat
                }
            }
            return null
        }
    }

    /**
     * Custom closeable SVG image that holds a single SVG.
     */
    class CloseableSvgImage(val svg: SVG) : CloseableImage() {
        private var isClose = false

        override fun close() {
            isClose = true
        }

        override fun getSizeInBytes(): Int = 0

        override fun isClosed(): Boolean = isClose

        override fun getWidth(): Int = 0

        override fun getHeight(): Int = 0
    }

    /**
     * Decodes a [SvgDecoder.svgFormat] image.
     */
    class Decoder : ImageDecoder {

        @Nullable override fun decode(encodedImage: EncodedImage, length: Int, qualityInfo: QualityInfo, options: ImageDecodeOptions): CloseableImage? {
            try {
                val svg = SVG.getFromInputStream(encodedImage.inputStream)
                return CloseableSvgImage(svg)
            } catch (e: SVGParseException) {
                e.printStackTrace()
            }
            // Return nothing if an error occurred
            return null
        }
    }

    /**
     * SVG drawable factory that creates [PictureDrawable]s for SVG images.
     */
    class SvgDrawableFactory : DrawableFactory {

        override fun supportsImageType(image: CloseableImage): Boolean {
            return image is CloseableSvgImage
        }

        @Nullable override fun createDrawable(image: CloseableImage): Drawable? {
            return SvgPictureDrawable((image as CloseableSvgImage).svg)
        }
    }

    class SvgPictureDrawable(private val svg: SVG) : PictureDrawable(null) {

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            picture = svg.renderToPicture(bounds.width(), bounds.height())
        }
    }
}