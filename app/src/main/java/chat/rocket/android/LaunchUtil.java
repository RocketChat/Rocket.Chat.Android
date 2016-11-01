package chat.rocket.android;

import android.content.Context;
import android.content.Intent;

import chat.rocket.android.activity.ServerConfigActivity;

public class LaunchUtil {
    public static void showServerConfigActivity(Context context, String id) {
        Intent intent = new Intent(context, ServerConfigActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }
}
