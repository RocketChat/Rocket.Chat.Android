package chat.rocket.persistence.realm.models.internal;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.SyncState;

/**
 * just stores gcm registration status.
 */
public class GcmPushRegistration extends RealmObject {
  @SuppressWarnings({"PMD.ShortVariable"})
  private static final String ID = "dummyId";
  public static final String SYNC_STATE = "syncState";
  public static final String GCM_PUSH_ENABLED = "gcmPushEnabled";
  private static final int DEFAULT_ID = 0;

  @PrimaryKey private int dummyId;
  private int syncState;
  private boolean gcmPushEnabled;

  public boolean isGcmPushEnabled() {
    return gcmPushEnabled;
  }

  public void setGcmPushEnabled(boolean gcmPushEnabled) {
    this.gcmPushEnabled = gcmPushEnabled;
  }

  public int getSyncState() {
    return syncState;
  }

  public void setSyncState(int syncState) {
    this.syncState = syncState;
  }

  public static GcmPushRegistration updateGcmPushEnabled(Realm realm, boolean gcmPushEnabled)
      throws JSONException {
    GcmPushRegistration gcmPushRegistration = GcmPushRegistration.queryDefault(realm).findFirst();
    if (gcmPushRegistration != null
        && (gcmPushRegistration.getSyncState() == SyncState.NOT_SYNCED
         || gcmPushRegistration.getSyncState() == SyncState.SYNCING)
        && gcmPushEnabled == gcmPushRegistration.isGcmPushEnabled()) {
      // omit duplicated request.
      return gcmPushRegistration;
    }

    return realm.createOrUpdateObjectFromJson(GcmPushRegistration.class, new JSONObject()
        .put(ID, DEFAULT_ID)
        .put(SYNC_STATE, SyncState.NOT_SYNCED)
        .put(GCM_PUSH_ENABLED, gcmPushEnabled));
  }

  public static RealmQuery<GcmPushRegistration> queryDefault(Realm realm) {
    return realm.where(GcmPushRegistration.class).equalTo(ID, DEFAULT_ID);
  }
}
