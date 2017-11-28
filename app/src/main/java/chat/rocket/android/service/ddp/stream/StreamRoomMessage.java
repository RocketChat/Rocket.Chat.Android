package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import io.realm.RealmObject;

/**
 * stream-room-message subscriber.
 */
public class StreamRoomMessage extends AbstractStreamNotifyEventSubscriber {
  private String roomId;

  public StreamRoomMessage(Context context, String hostname,
                           RealmHelper realmHelper, String roomId) {
    super(context, hostname, realmHelper);
    this.roomId = roomId;
  }

  @Override
  protected String getSubscriptionName() {
    return "stream-room-messages";
  }

  @Override
  protected String getSubscriptionParam() {
    return roomId;
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RealmMessage.class;
  }

  @Override
  protected String getPrimaryKeyForModel() {
    return "_id";
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    return RealmMessage.customizeJson(super.customizeFieldJson(json));
  }
}
