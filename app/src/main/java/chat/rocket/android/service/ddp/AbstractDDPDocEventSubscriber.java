package chat.rocket.android.service.ddp;

import android.content.Context;
import android.text.TextUtils;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.Registerable;
import chat.rocket.android_ddp.DDPSubscription;
import io.realm.Realm;
import io.realm.RealmObject;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;

public abstract class AbstractDDPDocEventSubscriber implements Registerable {
  protected final Context context;
  protected final String hostname;
  protected final RealmHelper realmHelper;
  protected final DDPClientWraper ddpClient;
  private boolean isUnsubscribed;
  private String subscriptionId;
  private Subscription rxSubscription;

  protected AbstractDDPDocEventSubscriber(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    this.context = context;
    this.hostname = hostname;
    this.realmHelper = realmHelper;
    this.ddpClient = ddpClient;
  }

  protected abstract String getSubscriptionName();

  protected abstract JSONArray getSubscriptionParams() throws JSONException;

  protected boolean shouldTruncateTableOnInitialize() {
    return false;
  }

  protected abstract boolean isTarget(String callbackName);

  protected abstract Class<? extends RealmObject> getModelClass();

  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    return json;
  }

  protected void onRegister() {}

  protected void onUnregister() {}

  @Override public final void register() {
    isUnsubscribed = false;
    JSONArray params = null;
    try {
      params = getSubscriptionParams();
    } catch (JSONException exception) {
      // just ignore.
    }

    ddpClient.subscribe(getSubscriptionName(), params).onSuccess(task -> {
      if (isUnsubscribed) {
        ddpClient.unsubscribe(task.getResult().id).continueWith(new LogcatIfError());
      } else {
        subscriptionId = task.getResult().id;
      }
      return null;
    }).continueWith(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError(), "DDP subscription failed.");
      }
      return null;
    });

    if (shouldTruncateTableOnInitialize()) {
      realmHelper.executeTransaction(realm -> {
        realm.delete(getModelClass());
        return null;
      }).onSuccess(task -> {
        rxSubscription = subscribe();
        return null;
      }).continueWith(new LogcatIfError());
    } else {
      rxSubscription = subscribe();
    }
    onRegister();
  }

  protected Subscription subscribe() {
    return ddpClient.getSubscriptionCallback()
        .filter(event -> event instanceof DDPSubscription.DocEvent)
        .cast(DDPSubscription.DocEvent.class)
        .filter(event -> isTarget(event.collection))
        .subscribe(docEvent -> {
          try {
            if (docEvent instanceof DDPSubscription.Added.Before) {
              onDocumentAdded((DDPSubscription.Added) docEvent); //ignore Before
            } else if (docEvent instanceof DDPSubscription.Added) {
              onDocumentAdded((DDPSubscription.Added) docEvent);
            } else if (docEvent instanceof DDPSubscription.Removed) {
              onDocumentRemoved((DDPSubscription.Removed) docEvent);
            } else if (docEvent instanceof DDPSubscription.Changed) {
              onDocumentChanged((DDPSubscription.Changed) docEvent);
            } else if (docEvent instanceof DDPSubscription.MovedBefore) {
              //ignore movedBefore
            }
          } catch (Exception exception) {
            RCLog.w(exception, "failed to handle subscription callback");
          }
        });
  }

  protected void onDocumentAdded(DDPSubscription.Added docEvent) {
    realmHelper.executeTransaction(realm -> {
      onDocumentAdded(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void onDocumentAdded(Realm realm, DDPSubscription.Added docEvent) throws JSONException {
    //executed in RealmTransaction
    JSONObject json = new JSONObject().put("_id", docEvent.docID);
    mergeJson(json, docEvent.fields);
    realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJson(json));
  }

  protected void onDocumentChanged(DDPSubscription.Changed docEvent) {
    realmHelper.executeTransaction(realm -> {
      onDocumentChanged(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void onDocumentChanged(Realm realm, DDPSubscription.Changed docEvent)
      throws JSONException {
    //executed in RealmTransaction
    JSONObject json = new JSONObject().put("_id", docEvent.docID);
    if (docEvent.cleared != null) {
      for (int i = 0; i < docEvent.cleared.length(); i++) {
        String fieldToDelete = docEvent.cleared.getString(i);
        json.put(fieldToDelete, JSONObject.NULL);
      }
    }
    mergeJson(json, docEvent.fields);
    realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJson(json));
  }

  protected void onDocumentRemoved(DDPSubscription.Removed docEvent) {
    realmHelper.executeTransaction(realm -> {
      onDocumentRemoved(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void onDocumentRemoved(Realm realm, DDPSubscription.Removed docEvent)
      throws JSONException {
    //executed in RealmTransaction
    realm.where(getModelClass()).equalTo("_id", docEvent.docID).findAll().deleteAllFromRealm();
  }

  private void mergeJson(JSONObject target, JSONObject src) throws JSONException {
    Iterator<String> iterator = src.keys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      target.put(key, src.get(key));
    }
  }

  @Override public final void unregister() {
    isUnsubscribed = true;
    onUnregister();
    if (rxSubscription != null) {
      rxSubscription.unsubscribe();
    }
    if (!TextUtils.isEmpty(subscriptionId)) {
      ddpClient.unsubscribe(subscriptionId).continueWith(new LogcatIfError());
    }
  }
}
