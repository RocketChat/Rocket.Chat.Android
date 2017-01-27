package chat.rocket.android.widget;

import android.content.Context;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import chat.rocket.android.widget.fresco.FrescoConfiguration;
import chat.rocket.android.widget.fresco.ImageFormatConfigurator;
import okhttp3.OkHttpClient;

public class RocketChatWidgets {

  public static void initialize(Context context, OkHttpClient okHttpClient) {
    FrescoConfiguration.initialize(context, okHttpClient);
  }
}
