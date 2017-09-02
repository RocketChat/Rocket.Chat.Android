package chat.rocket.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import chat.rocket.android.log.RCLog;
import chat.rocket.core.utils.Pair;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  private static final String KEY_SELECTED_SERVER_HOSTNAME = "KEY_SELECTED_SERVER_HOSTNAME";
  private static final String KEY_SELECTED_ROOM_ID = "KEY_SELECTED_ROOM_ID";
  private static final String KEY_PUSH_ID = "KEY_PUSH_ID";
  private static final String KEY_HOSTNAME_LIST = "KEY_HOSTNAME_LIST";

  private Context context;

  public RocketChatCache(Context context) {
    this.context = context.getApplicationContext();
  }

  public String getSelectedServerHostname() {
    return getString(KEY_SELECTED_SERVER_HOSTNAME, null);
  }

  public void setSelectedServerHostname(String hostname) {
    setString(KEY_SELECTED_SERVER_HOSTNAME, hostname.toLowerCase());
  }

  public void addHostname(@NonNull String hostname, @Nullable String hostnameAvatarUri) {
    String hostnameList = getString(KEY_HOSTNAME_LIST, null);
    try {
      JSONObject json;
      if (hostnameList == null) {
        json = new JSONObject();
      } else {
        json = new JSONObject(hostnameList);
      }
      // Replace server avatar uri if exists.
      json.put(hostname, hostnameAvatarUri == null ? JSONObject.NULL : hostnameAvatarUri);
      setString(KEY_HOSTNAME_LIST, json.toString());
    } catch (JSONException e) {
      RCLog.e(e);
    }
  }

  public List<Pair<String, String>> getServerList() {
    String json = getString(KEY_HOSTNAME_LIST, null);
    if (json == null) {
      return Collections.emptyList();
    }
    try {
      JSONObject jsonObj = new JSONObject(json);
      List<Pair<String, String>> serverList = new ArrayList<>();
      for (Iterator<String> iter = jsonObj.keys(); iter.hasNext();) {
        String hostname = iter.next();
        serverList.add(new Pair<>(hostname,"http://" + hostname + "/" + jsonObj.getString(hostname)));
      }
      return serverList;
    } catch (JSONException e) {
      RCLog.e(e);
    }
    return Collections.emptyList();
  }

  public String getSelectedRoomId() {
    return getString(getSelectedServerHostname() + KEY_SELECTED_ROOM_ID, null);
  }

  public void setSelectedRoomId(String roomId) {
    setString(getSelectedServerHostname() + KEY_SELECTED_ROOM_ID, roomId);
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
    return getValuePublisher(getSelectedServerHostname() + KEY_SELECTED_ROOM_ID);
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences("cache", Context.MODE_PRIVATE);
  }

  private SharedPreferences.Editor getEditor() {
    return getSharedPreferences().edit();
  }

  public String getString(String key, String defaultValue) {
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
          emitter.onNext(Optional.ofNullable(value));
        }
      };

      emitter.setCancellable(() -> getSharedPreferences()
          .unregisterOnSharedPreferenceChangeListener(listener));

      getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }, BackpressureStrategy.LATEST);
  }
}
