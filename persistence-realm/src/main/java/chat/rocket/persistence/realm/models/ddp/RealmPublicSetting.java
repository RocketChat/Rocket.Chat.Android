package chat.rocket.persistence.realm.models.ddp;

import android.support.annotation.Nullable;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.persistence.realm.RealmHelper;

/**
 * public setting model.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmPublicSetting extends RealmObject {

  public static final String ID = "_id";
  public static final String GROUP = "group";
  public static final String TYPE = "type";
  public static final String VALUE = "value";
  public static final String UPDATED_AT = "_updatedAt";
  public static final String META = "meta";

  @PrimaryKey private String _id;
  private String group;
  private String type;
  private String value; //any type is available...!
  private long _updatedAt;
  private String meta; //JSON

  public static JSONObject customizeJson(JSONObject settingJson) throws JSONException {
    if (!settingJson.isNull(UPDATED_AT)) {
      long updatedAt = settingJson.getJSONObject(UPDATED_AT)
          .getLong(JsonConstants.DATE);
      settingJson.remove(UPDATED_AT);
      settingJson.put(UPDATED_AT, updatedAt);
    }

    return settingJson;
  }

  @Nullable
  private static RealmPublicSetting get(RealmHelper realmHelper, String _id) {
    return realmHelper.executeTransactionForRead(realm ->
        realm.where(RealmPublicSetting.class).equalTo(ID, _id).findFirst());
  }

  @Nullable
  public static String getString(RealmHelper realmHelper,
                                 String _id, String defaultValue) {
    RealmPublicSetting setting = get(realmHelper, _id);
    if (setting != null) {
      return setting.getValue();
    }
    return defaultValue;
  }

  public static boolean getBoolean(RealmHelper realmHelper,
                                   String _id, boolean defaultValue) {
    RealmPublicSetting setting = get(realmHelper, _id);
    if (setting != null) {
      return Boolean.parseBoolean(setting.getValue());
    }
    return defaultValue;
  }

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
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

  public long getUpdatedAt() {
    return _updatedAt;
  }

  public void setUpdatedAt(long _updatedAt) {
    this._updatedAt = _updatedAt;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }

  public PublicSetting asPublicSetting() {
    return PublicSetting.builder()
        .setId(_id)
        .setGroup(group)
        .setType(type)
        .setValue(value)
        .setUpdatedAt(_updatedAt)
        .build();
  }
}
