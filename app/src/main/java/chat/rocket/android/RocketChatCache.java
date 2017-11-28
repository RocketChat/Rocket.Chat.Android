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

import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.utils.Pair;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.HttpUrl;

/**
 * sharedpreference-based cache.
 */
public class RocketChatCache {
  private static final String KEY_SELECTED_SERVER_HOSTNAME = "KEY_SELECTED_SERVER_HOSTNAME";
  private static final String KEY_SELECTED_SITE_URL = "KEY_SELECTED_SITE_URL";
  private static final String KEY_SELECTED_SITE_NAME = "KEY_SELECTED_SITE_NAME";
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
    String newHostname = null;
    if (hostname != null) {
      newHostname = hostname.toLowerCase();
    }
    setString(KEY_SELECTED_SERVER_HOSTNAME, newHostname);
  }

  public void addHostSiteName(@NonNull String currentHostname, @NonNull String siteName) {
    try {
      String hostSiteNamesJson = getHostSiteNamesJson();
      JSONObject jsonObject = (hostSiteNamesJson == null) ?
              new JSONObject() : new JSONObject(hostSiteNamesJson);
      jsonObject.put(currentHostname, siteName);
      setString(KEY_SELECTED_SITE_NAME, jsonObject.toString());
    } catch (JSONException e) {
      RCLog.e(e);
    }
  }

  public @NonNull String getHostSiteName(@NonNull String host) {
    if (host.startsWith("http")) {
      HttpUrl url = HttpUrl.parse(host);
      if (url != null) {
        host = url.host();
      }
    }
    try {
      String hostSiteNamesJson = getHostSiteNamesJson();
      JSONObject jsonObject = (hostSiteNamesJson == null) ?
              new JSONObject() : new JSONObject(hostSiteNamesJson);
      host = getSiteUrlFor(host);
      return jsonObject.optString(host);
    } catch (JSONException e) {
      RCLog.e(e);
    }
    return "";
  }

  private @Nullable String getHostSiteNamesJson() {
    return getString(KEY_SELECTED_SITE_NAME, null);
  }

  public void addHostnameSiteUrl(@Nullable String hostnameAlias, @NonNull String currentHostname) {
    String alias = null;
    if (hostnameAlias != null) {
      alias = hostnameAlias.toLowerCase();
    }
    try {
      String selectedHostnameAliasJson = getLoginHostnamesJson();
      JSONObject jsonObject = selectedHostnameAliasJson == null ?
              new JSONObject() : new JSONObject(selectedHostnameAliasJson);
      jsonObject.put(alias, currentHostname);
      setString(KEY_SELECTED_SITE_URL, jsonObject.toString());
    } catch (JSONException e) {
      RCLog.e(e);
    }
  }

  public @Nullable String getSiteUrlFor(String hostname) {
    try {
      String selectedServerHostname = getSelectedServerHostname();
      if (getLoginHostnamesJson() == null || getLoginHostnamesJson().isEmpty()) {
        return null;
      }
      return new JSONObject(getLoginHostnamesJson())
              .optString(hostname, selectedServerHostname);
    } catch (JSONException e) {
      RCLog.e(e);
    }
    return null;
  }

  private @Nullable String getLoginHostnamesJson() {
    return getString(KEY_SELECTED_SITE_URL, null);
  }

  public void addHostname(@NonNull String hostname, @Nullable String hostnameAvatarUri, String siteName) {
    String hostnameList = getString(KEY_HOSTNAME_LIST, null);
    try {
      JSONObject json;
      if (hostnameList == null) {
        json = new JSONObject();
      } else {
        json = new JSONObject(hostnameList);
      }
      JSONObject serverInfoJson = new JSONObject();
      serverInfoJson.put("avatar", hostnameAvatarUri);
      serverInfoJson.put("sitename", siteName);
      // Replace server avatar uri if exists.
      json.put(hostname, hostnameAvatarUri == null ? JSONObject.NULL : serverInfoJson);
      setString(KEY_HOSTNAME_LIST, json.toString());
    } catch (JSONException e) {
      RCLog.e(e);
    }
  }

  public List<Pair<String, Pair<String, String>>> getServerList() {
    String json = getString(KEY_HOSTNAME_LIST, null);
    if (json == null) {
      return Collections.emptyList();
    }
    try {
      JSONObject jsonObj = new JSONObject(json);
      List<Pair<String, Pair<String, String>>> serverList = new ArrayList<>();
      for (Iterator<String> iter = jsonObj.keys(); iter.hasNext();) {
        String hostname = iter.next();
        JSONObject serverInfoJson = jsonObj.getJSONObject(hostname);
        serverList.add(new Pair<>(hostname, new Pair<>(
                "http://" + hostname + "/" + serverInfoJson.getString("avatar"),
                serverInfoJson.getString("sitename"))));
      }
      return serverList;
    } catch (JSONException e) {
      RCLog.e(e);
    }
    return Collections.emptyList();
  }

  public void removeHostname(String hostname) {
    String json = getString(KEY_HOSTNAME_LIST, null);
    if (TextUtils.isEmpty(json)) {
      return;
    }
    try {
      JSONObject jsonObj = new JSONObject(json);
      jsonObj.remove(hostname);
      String result = jsonObj.length() == 0 ? null : jsonObj.toString();
      setString(KEY_HOSTNAME_LIST, result);
    } catch (JSONException e) {
      RCLog.e(e);
    }
  }

  @Nullable
  public String getFirstLoggedHostnameIfAny() {
    String json = getString(KEY_HOSTNAME_LIST, null);
    if (json != null) {
      try {
        JSONObject jsonObj = new JSONObject(json);
        if (jsonObj.length() > 0 && jsonObj.keys().hasNext()) {
          // Returns the first hostname on the list.
          return jsonObj.keys().next();
        }
      } catch (JSONException e) {
        RCLog.e(e);
      }
    }
    return null;
  }

  public String getSelectedRoomId() {
    try {
      JSONObject jsonObject = getSelectedRoomIdJsonObject();
      return jsonObject.optString(getSelectedServerHostname(), null);
    } catch (JSONException e) {
      RCLog.e(e);
      Logger.report(e);
    }
    return null;
  }

  public void setSelectedRoomId(String roomId) {
    try {
      JSONObject jsonObject = getSelectedRoomIdJsonObject();
      jsonObject.put(getSelectedServerHostname(), roomId);
      setString(KEY_SELECTED_ROOM_ID, jsonObject.toString());
    } catch (JSONException e) {
      RCLog.e(e);
      Logger.report(e);
    }
  }

  private JSONObject getSelectedRoomIdJsonObject() throws JSONException {
    String json = getString(KEY_SELECTED_ROOM_ID, null);
    if (json == null) {
      return new JSONObject();
    }
    return new JSONObject(json);
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
    return getValuePublisher(KEY_SELECTED_ROOM_ID)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(roomValue -> Optional.ofNullable(new JSONObject(roomValue).optString(getSelectedServerHostname(), null)));
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

  public void removeSelectedRoomId(String currentHostname) {
    try {
      JSONObject selectedRoomIdJsonObject = getSelectedRoomIdJsonObject();
      selectedRoomIdJsonObject.remove(currentHostname);
      String result = selectedRoomIdJsonObject.length() == 0 ?
              null : selectedRoomIdJsonObject.toString();
      setString(KEY_SELECTED_ROOM_ID, result);
    } catch (JSONException e) {
      Logger.report(e);
      RCLog.e(e);
    }
  }
}
