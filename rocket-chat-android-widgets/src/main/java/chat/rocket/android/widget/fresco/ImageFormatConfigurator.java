package chat.rocket.android.widget.fresco;

import android.support.annotation.Nullable;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;

/**
 * Based on https://github.com/facebook/fresco/blob/master/samples/showcase/src/main/java/com/facebook/fresco/samples/showcase/CustomImageFormatConfigurator.java
 */
public class ImageFormatConfigurator {

  @Nullable
  public static ImageDecoderConfig createImageDecoderConfig() {
    ImageDecoderConfig.Builder config = ImageDecoderConfig.newBuilder();

    config.addDecodingCapability(
        SvgDecoderConfig.SVG_FORMAT,
        new SvgDecoderConfig.SvgFormatChecker(),
        new SvgDecoderConfig.SvgDecoder());

    return config.build();
  }

  public static void addCustomDrawableFactories(DraweeConfig.Builder draweeConfigBuilder) {
    draweeConfigBuilder.addCustomDrawableFactory(new SvgDecoderConfig.SvgDrawableFactory());
  }
}
