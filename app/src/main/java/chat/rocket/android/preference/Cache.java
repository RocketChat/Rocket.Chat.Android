package chat.rocket.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class Cache {
    public static final String KEY_MY_USER_ID = "my_user_id";
    public static final String KEY_MY_USER_NAME = "my_user_name";
    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences("cache",Context.MODE_PRIVATE);
    }

    public interface ValueCallback<T> {
        void onGetValue(T value);
    }
    public static void waitForValue(Context context, @NonNull final String targetKey, final ValueCallback<String> callback) {
        Cache.get(context).registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (targetKey.equals(key)) {
                    String value = sharedPreferences.getString(key,"");
                    if(!TextUtils.isEmpty(value)) {
                        callback.onGetValue(value);
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                    }
                }
            }
        });
    }
}
