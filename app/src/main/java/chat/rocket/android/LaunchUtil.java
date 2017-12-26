package chat.rocket.android;

import android.content.Context;
import android.content.Intent;

import chat.rocket.android.activity.AddServerActivity;
import chat.rocket.android.activity.LoginActivity;
import chat.rocket.android.activity.MainActivity;

/**
 * utility class for launching Activity.
 */
public class LaunchUtil {

    /**
     * launch MainActivity with proper flags.
     */
    public static void showMainActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * launch AddServerActivity with proper flags.
     */
    public static void showAddServerActivity(Context context) {
        Intent intent = new Intent(context, AddServerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    /**
     * launch ServerConfigActivity with proper flags.
     */
    public static void showLoginActivity(Context context, String hostname) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(LoginActivity.KEY_HOSTNAME, hostname);
        context.startActivity(intent);
    }
}
