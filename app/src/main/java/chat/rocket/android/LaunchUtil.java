package chat.rocket.android;

import android.content.Context;
import android.content.Intent;
import chat.rocket.android.activity.ServerConfigActivity;

/**
 * utility class for launching Activity.
 */
public class LaunchUtil {
  /**
   * launch ServerConfigActivity with proper flags.
   */
  public static void showServerConfigActivity(Context context, String serverCondigId) {
    Intent intent = new Intent(context, ServerConfigActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("id", serverCondigId);
    context.startActivity(intent);
  }
}
