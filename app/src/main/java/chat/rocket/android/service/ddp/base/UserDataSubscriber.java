package chat.rocket.android.service.ddp.base;

import android.content.Context;
import io.realm.RealmObject;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;

/**
 * "userData" subscriber.
 */
public class UserDataSubscriber extends AbstractBaseSubscriber {
  public UserDataSubscriber(Context context, String hostname, RealmHelper realmHelper,
                            DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override
  protected String getSubscriptionName() {
    return "userData";
  }

  @Override
  protected String getSubscriptionCallbackName() {
    return "users";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return User.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    json = super.customizeFieldJson(json);

    // The user object may have some children without a proper primary key (ex.: settings)
    // Here we identify this and add a local key
    if (json.has("settings")) {
      final JSONObject settingsJson = json.getJSONObject("settings");
      settingsJson.put("id", json.getString("_id"));

      if (settingsJson.has("preferences")) {
        final JSONObject preferencesJson = settingsJson.getJSONObject("preferences");
        preferencesJson.put("id", json.getString("_id"));
      }
    }

    return json;
  }
}
