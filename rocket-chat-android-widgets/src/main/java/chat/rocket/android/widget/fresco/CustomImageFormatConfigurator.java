package chat.rocket.android.widget.fresco;

import android.support.annotation.Nullable;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;

/**
 * Helper class to add custom decoders and drawable factories.
 * See: https://github.com/facebook/fresco/blob/master/samples/showcase/src/main/java/com/facebook/fresco/samples/showcase/CustomImageFormatConfigurator.java
 */
public class CustomImageFormatConfigurator {

  @Nullable
  public static ImageDecoderConfig createImageDecoderConfig() {
    ImageDecoderConfig.Builder config = ImageDecoderConfig.newBuilder();
    config.addDecodingCapability(SvgDecoder.SVG_FORMAT, new SvgDecoder.SvgFormatChecker(), new SvgDecoder.Decoder());
    return config.build();
  }

  public static void addCustomDrawableFactories(DraweeConfig.Builder draweeConfigBuilder) {
    draweeConfigBuilder.addCustomDrawableFactory(ColorImage.createDrawableFactory());
    draweeConfigBuilder.addCustomDrawableFactory(new SvgDecoder.SvgDrawableFactory());
  }
}