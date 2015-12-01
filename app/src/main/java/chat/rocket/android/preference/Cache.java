package chat.rocket.android.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class Cache {
    public static final String KEY_MY_USER_ID = "my_user_id";
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("cache",Context.MODE_PRIVATE);
    }
}
