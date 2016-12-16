package chat.rocket.android.model.ddp;

import android.support.annotation.Nullable;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * public setting model.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class PublicSetting extends RealmObject {
  @PrimaryKey private String _id;
  private String group;
  private String type;
  private String value; //any type is available...!
  private long _updatedAt;
  private String meta; //JSON

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public long get_updatedAt() {
    return _updatedAt;
  }

  public void set_updatedAt(long _updatedAt) {
    this._updatedAt = _updatedAt;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }

  public static JSONObject customizeJson(JSONObject settingJson) throws JSONException {
    if (!settingJson.isNull("_updatedAt")) {
      long updatedAt = settingJson.getJSONObject("_updatedAt").getLong("$date");
      settingJson.remove("_updatedAt");
      settingJson.put("_updatedAt", updatedAt);
    }

    return settingJson;
  }

  private static @Nullable PublicSetting get(RealmHelper realmHelper, String _id) {
    return realmHelper.executeTransactionForRead(realm ->
        realm.where(PublicSetting.class).equalTo("_id", _id).findFirst());
  }

  public static @Nullable String getString(RealmHelper realmHelper,
      String _id, String defaultValue) {
    PublicSetting setting = get(realmHelper, _id);
    if (setting != null) {
      return setting.getValue();
    }
    return defaultValue;
  }

  public static @Nullable boolean getBoolean(RealmHelper realmHelper,
      String _id, boolean defaultValue) {
    PublicSetting setting = get(realmHelper, _id);
    if (setting != null) {
      return Boolean.parseBoolean(setting.getValue());
    }
    return defaultValue;
  }
}
