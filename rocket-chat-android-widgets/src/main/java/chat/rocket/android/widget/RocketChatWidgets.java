package chat.rocket.android.widget;

import android.content.Context;
import chat.rocket.android.widget.fresco.CustomImageFormatConfigurator;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import okhttp3.OkHttpClient;

public class RocketChatWidgets {

  public static void initialize(Context context, OkHttpClient okHttpClient) {
    ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
        .newBuilder(context, okHttpClient)
        .setDownsampleEnabled(true)
        .setImageDecoderConfig(CustomImageFormatConfigurator.createImageDecoderConfig())
        .build();

    DraweeConfig.Builder draweeConfigBuilder = DraweeConfig.newBuilder();
    CustomImageFormatConfigurator.addCustomDrawableFactories(draweeConfigBuilder);

    Fresco.initialize(context, config, draweeConfigBuilder.build());
  }
}