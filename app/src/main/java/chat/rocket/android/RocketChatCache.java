package chat.rocket.android;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  public static final String KEY_SELECTED_SERVER_CONFIG_ID = "selectedServerConfigId";
  public static final String KEY_SELECTED_ROOM_ID = "selectedRoomId";

  private static final String PUSH_ID = "pushId";

  /**
   * get SharedPreference instance for RocketChat application cache.
   */
  public static SharedPreferences get(Context context) {
    return context.getSharedPreferences("cache", Context.MODE_PRIVATE);
  }

  public static String getPushId(Context context) {
    SharedPreferences preferences = get(context);
    String pushId = null;
    if (!preferences.contains(PUSH_ID)) {
      // generates one and save
      pushId = UUID.randomUUID().toString();
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(PUSH_ID, pushId);
      editor.apply();
    }
    return preferences.getString(PUSH_ID, pushId);
  }
}
