package chat.rocket.android;

import android.content.Context;
import android.content.Intent;

import chat.rocket.android.activity.AddServerActivity;
import chat.rocket.android.activity.ServerConfigActivity;

/**
 * utility class for launching Activity.
 */
public class LaunchUtil {

  /**
   * launch AddServerActivity with proper flags.
   */
  public static void showAddServerActivity(Context context) {
    Intent intent = new Intent(context, AddServerActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    context.startActivity(intent);
  }

  /**
   * launch ServerConfigActivity with proper flags.
   */
  public static void showServerConfigActivity(Context context, String serverCondigId) {
    Intent intent = new Intent(context, ServerConfigActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("serverConfigId", serverCondigId);
    context.startActivity(intent);
  }
}
