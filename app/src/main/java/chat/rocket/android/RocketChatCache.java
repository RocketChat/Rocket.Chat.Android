package chat.rocket.android;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  public static final String KEY_SELECTED_SERVER_CONFIG_ID = "selectedServerConfigId";
  public static SharedPreferences get(Context context) {
    return context.getSharedPreferences("cache", Context.MODE_PRIVATE);
  }
}
