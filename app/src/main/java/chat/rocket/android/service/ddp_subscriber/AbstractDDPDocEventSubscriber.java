package chat.rocket.android.service.ddp_subscriber;

import android.content.Context;
import android.text.TextUtils;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPSubscription;
import io.realm.Realm;
import io.realm.RealmObject;
import java.util.Iterator;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;
import timber.log.Timber;

abstract class AbstractDDPDocEventSubscriber implements Registerable {
  protected final Context mContext;
  protected final RocketChatWebSocketAPI mAPI;
  private String mID;
  private Subscription mSubscription;

  protected AbstractDDPDocEventSubscriber(Context context, RocketChatWebSocketAPI api) {
    mContext = context;
    mAPI = api;
  }

  protected abstract String getSubscriptionName();

  protected abstract String getSubscriptionCallbackName();

  protected abstract Class<? extends RealmObject> getModelClass();

  protected JSONObject customizeFieldJSON(JSONObject json) {
    return json;
  }

  @Override public void register() {
    mAPI.subscribe(getSubscriptionName(), null).onSuccess(task -> {
      mID = task.getResult().id;
      return null;
    }).continueWith(task -> {
      if (task.isFaulted()) {
        Timber.w(task.getError(), "DDP subscription failed.");
      }
      return null;
    });

    RealmHelperBolts.executeTransaction(realm -> {
      realm.delete(getModelClass());
      return null;
    }).onSuccess(task -> {
      registerSubscriptionCallback();
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void registerSubscriptionCallback() {
    mSubscription = mAPI.getSubscriptionCallback()
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
          } catch (Exception e) {
            Timber.w(e, "failed to handle subscription callback");
          }
        });
  }

  protected void onDocumentAdded(DDPSubscription.Added docEvent) {
    RealmHelperBolts.executeTransaction(realm -> {
      onDocumentAdded(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  protected void onDocumentChanged(DDPSubscription.Changed docEvent) {
    RealmHelperBolts.executeTransaction(realm -> {
      onDocumentChanged(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  protected void onDocumentRemoved(DDPSubscription.Removed docEvent) {
    RealmHelperBolts.executeTransaction(realm -> {
      onDocumentRemoved(realm, docEvent);
      return null;
    }).continueWith(new LogcatIfError());
  }

  private void mergeJSON(JSONObject target, JSONObject src) throws JSONException {
    Iterator<String> iterator = src.keys();
    while (iterator.hasNext()) {
      String key = iterator.next();
      target.put(key, src.get(key));
    }
  }

  private void onDocumentAdded(Realm realm, DDPSubscription.Added docEvent) throws JSONException {
    //executed in RealmTransaction
    JSONObject json = new JSONObject().put("id", docEvent.docID);
    mergeJSON(json, docEvent.fields);
    realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJSON(json));
  }

  private void onDocumentChanged(Realm realm, DDPSubscription.Changed docEvent)
      throws JSONException {
    //executed in RealmTransaction
    JSONObject json = new JSONObject().put("id", docEvent.docID);
    for (int i = 0; i < docEvent.cleared.length(); i++) {
      String fieldToDelete = docEvent.cleared.getString(i);
      json.remove(fieldToDelete);
    }
    mergeJSON(json, docEvent.fields);
    realm.createOrUpdateObjectFromJson(getModelClass(), customizeFieldJSON(json));
  }

  private void onDocumentRemoved(Realm realm, DDPSubscription.Removed docEvent)
      throws JSONException {
    //executed in RealmTransaction
    realm.where(getModelClass()).equalTo("id", docEvent.docID).findAll().deleteAllFromRealm();
  }

  @Override public void keepalive() {

  }

  @Override public void unregister() {
    if (mSubscription != null) {
      mSubscription.unsubscribe();
    }
    if (!TextUtils.isEmpty(mID)) {
      mAPI.unsubscribe(mID).continueWith(new LogcatIfError());
    }
  }
}
