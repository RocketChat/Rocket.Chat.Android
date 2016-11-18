package chat.rocket.android.service.ddp;

import android.content.Context;
import android.text.TextUtils;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.api.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPSubscription;
import io.realm.Realm;
import io.realm.RealmObject;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;
import timber.log.Timber;

abstract class AbstractDDPDocEventSubscriber implements Registerable {
  protected final Context context;
  protected final RealmHelper realmHelper;
  protected final RocketChatWebSocketAPI webSocketAPI;
  private String subscriptionId;
  private Subscription rxSubscription;

  protected AbstractDDPDocEventSubscriber(Context context, RealmHelper realmHelper,
      RocketChatWebSocketAPI api) {
    this.context = context;
    this.realmHelper = realmHelper;
    this.webSocketAPI = api;
  }

  protected abstract String getSubscriptionName();

  protected abstract String getSubscriptionCallbackName();

  protected abstract Class<? extends RealmObject> getModelClass();

  protected JSONObject customizeFieldJson(JSONObject json) {
    return json;
  }

  @Override public void register() {
    webSocketAPI.subscribe(getSubscriptionName(), null).onSuccess(task -> {
      subscriptionId = task.getResult().id;
      return null;
    }).continueWith(task -> {
      if (task.isFaulted()) {
        Timber.w(task.getError(), "DDP subscription failed.");
      }
      return null;
    });

    realmHelper.executeTransaction(realm -> {
      realm.delete(getModelClass());
      return null;
    }).onSuccess(task -> {
      registerSubscriptionCallback();
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void registerSubscriptionCallback() {
    rxSubscription = webSocketAPI.getSubscriptionCallback()
        .filter(event -> event instanceof DDPSubscription.DocEvent)
        .cast(DDPSubscription.DocEvent.class)
        .filter(event -> getSubscriptionCallbackName().equals(event.collection))
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
            Timber.w(exception, "failed to handle subscription callback");
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
    for (int i = 0; i < docEvent.cleared.length(); i++) {
      String fieldToDelete = docEvent.cleared.getString(i);
      json.put(fieldToDelete, JSONObject.NULL);
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

  @Override public void keepalive() {

  }

  @Override public void unregister() {
    if (rxSubscription != null) {
      rxSubscription.unsubscribe();
    }
    if (!TextUtils.isEmpty(subscriptionId)) {
      webSocketAPI.unsubscribe(subscriptionId).continueWith(new LogcatIfError());
    }
  }
}
