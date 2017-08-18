package chat.rocket.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.hadisatrio.optional.Optional;

import java.util.UUID;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  private static final String KEY_SELECTED_SERVER_HOSTNAME = "selectedServerHostname";
  private static final String KEY_SELECTED_ROOM_ID = "selectedRoomId";
  private static final String KEY_PUSH_ID = "pushId";

  private Context context;

  public RocketChatCache(Context context) {
    this.context = context.getApplicationContext();
  }

  public String getSelectedServerHostname() {
    return getString(KEY_SELECTED_SERVER_HOSTNAME, null);
  }

  public void setSelectedServerHostname(String hostname) {
    setString(KEY_SELECTED_SERVER_HOSTNAME, hostname);
  }

  public String getSelectedRoomId() {
    return getString(KEY_SELECTED_ROOM_ID, null);
  }

  public void setSelectedRoomId(String roomId) {
    setString(KEY_SELECTED_ROOM_ID, roomId);
  }

  public String getOrCreatePushId() {
    SharedPreferences preferences = getSharedPreferences();
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

  public Flowable<Optional<String>> getSelectedServerHostnamePublisher() {
    return getValuePublisher(KEY_SELECTED_SERVER_HOSTNAME);
  }

  public Flowable<Optional<String>> getSelectedRoomIdPublisher() {
    return getValuePublisher(KEY_SELECTED_ROOM_ID);
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences("cache", Context.MODE_PRIVATE);
  }

  private SharedPreferences.Editor getEditor() {
    return getSharedPreferences().edit();
  }

  private String getString(String key, String defaultValue) {
    return getSharedPreferences().getString(key, defaultValue);
  }

  private void setString(String key, String value) {
    getEditor().putString(key, value).apply();
  }

  private Flowable<Optional<String>> getValuePublisher(final String key) {
    return Flowable.create(emitter -> {
      SharedPreferences.OnSharedPreferenceChangeListener
          listener = (sharedPreferences, changedKey) -> {
        if (key.equals(changedKey) && !emitter.isCancelled()) {
          String value = getString(key, null);
          emitter.onNext(Optional.of(value));
        }
      };

      emitter.setCancellable(() -> getSharedPreferences()
          .unregisterOnSharedPreferenceChangeListener(listener));

      getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }, BackpressureStrategy.LATEST);
  }
}
