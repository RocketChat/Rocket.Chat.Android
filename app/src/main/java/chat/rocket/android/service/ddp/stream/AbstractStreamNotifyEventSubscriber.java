package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.ddp.AbstractDDPDocEventSubscriber;
import chat.rocket.android_ddp.DDPSubscription;
import org.json.JSONArray;
import org.json.JSONObject;
import timber.log.Timber;

abstract class AbstractStreamNotifyEventSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractStreamNotifyEventSubscriber(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, realmHelper, ddpClient);
  }

  @Override protected final boolean shouldTruncateTableOnInitialize() {
    return false;
  }

  @Override protected final boolean isTarget(String callbackName) {
    return getSubscriptionName().equals(callbackName);
  }

  protected abstract String getPrimaryKeyForModel();

  @Override protected void onDocumentChanged(DDPSubscription.Changed docEvent) {
    try {
      JSONArray args = docEvent.fields.getJSONArray("args");
      String msg = args.getString(0);
      JSONObject target = args.getJSONObject(1);
      if ("removed".equals(msg)) {
        realmHelper.executeTransaction(realm ->
            realm.where(getModelClass())
                .equalTo(getPrimaryKeyForModel(), target.getString(getPrimaryKeyForModel()))
                .findAll().deleteAllFromRealm()
        ).continueWith(new LogcatIfError());
      } else { //inserted, updated
        realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJson(target))
        ).continueWith(new LogcatIfError());
      }
    } catch (Exception exception) {
      Timber.w(exception, "failed to save stream-notify event.");
    }
  }
}
