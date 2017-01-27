package chat.rocket.android.widget;

import android.content.Context;

import chat.rocket.android.widget.fresco.FrescoConfiguration;
import okhttp3.OkHttpClient;

public class RocketChatWidgets {

  public static void initialize(Context context, OkHttpClient okHttpClient) {
    FrescoConfiguration.initialize(context, okHttpClient);
  }
}
