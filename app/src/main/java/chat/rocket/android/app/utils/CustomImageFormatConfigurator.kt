package chat.rocket.android.app.utils

import com.facebook.drawee.backends.pipeline.DraweeConfig
import com.facebook.imagepipeline.decoder.ImageDecoderConfig

/**
 * Utility class to add custom decoders and drawable factories.
 *
 * @see {https://github.com/facebook/fresco/blob/master/samples/showcase/src/main/java/com/facebook/fresco/samples/showcase/CustomImageFormatConfigurator.java}
 */
object CustomImageFormatConfigurator {

    fun createImageDecoderConfig() : ImageDecoderConfig {
        return ImageDecoderConfig.newBuilder()
                .addDecodingCapability(SvgDecoder.svgFormat, SvgDecoder.SvgFormatChecker(), SvgDecoder.Decoder())
                .build()
    }

    fun addCustomDrawableFactories(draweeConfigBuilder: DraweeConfig.Builder) {
        // We always add the color drawable factory so that it can be used for image decoder overrides.
        draweeConfigBuilder.addCustomDrawableFactory(ColorImage.createDrawableFactory())
        draweeConfigBuilder.addCustomDrawableFactory(SvgDecoder.SvgDrawableFactory())
    }
}