package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * stream-room-message subscriber.
 */
public class StreamRoomMessage extends AbstractStreamNotifyEventSubscriber {
  private String roomId;

  public StreamRoomMessage(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient, String roomId) {
    super(context, hostname, realmHelper, ddpClient);
    this.roomId = roomId;
  }

  @Override protected String getSubscriptionName() {
    return "stream-room-messages";
  }

  @Override protected String getSubscriptionParam() {
    return roomId;
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return Message.class;
  }

  @Override protected String getPrimaryKeyForModel() {
    return "_id";
  }

  @Override protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    return Message.customizeJson(super.customizeFieldJson(json));
  }
}
