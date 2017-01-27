package chat.rocket.android;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  public static final String KEY_SELECTED_SERVER_HOSTNAME = "selectedServerHostname";
  public static final String KEY_SELECTED_ROOM_ID = "selectedRoomId";

  private static final String KEY_PUSH_ID = "pushId";

  /**
   * get SharedPreference instance for RocketChat application cache.
   */
  public static SharedPreferences get(Context context) {
    return context.getSharedPreferences("cache", Context.MODE_PRIVATE);
  }

  public static String getSelectedServerHostname(Context context) {
    return get(context).getString(KEY_SELECTED_SERVER_HOSTNAME, null);
  }

  public static String getOrCreatePushId(Context context) {
    SharedPreferences preferences = get(context);
    if (!preferences.contains(KEY_PUSH_ID)) {
      // generates one and save
      String newId = UUID.randomUUID().toString().replace("-", "");
      preferences.edit()
          .putString(KEY_PUSH_ID, newId)
          .apply();
      return newId;
    }
    return preferences.getString(KEY_PUSH_ID, null);
  }
}
