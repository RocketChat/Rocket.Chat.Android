package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.api.RaixPushHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.Registrable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import hugo.weaving.DebugLog;

/**
 * observe the user with emails.
 */
public class CurrentUserObserver extends AbstractModelObserver<User> {
  private final MethodCallHelper methodCall;
  private boolean currentUserExists;
  private ArrayList<Registrable> listeners;

  public CurrentUserObserver(Context context, String hostname,
                             RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
    currentUserExists = false;
  }

  @Override
  public RealmResults<User> queryItems(Realm realm) {
    return User.queryCurrentUser(realm).findAll();
  }

  @Override
  public void onUpdateResults(List<User> results) {
    boolean exists = !results.isEmpty();

    if (currentUserExists != exists) {
      if (exists) {
        onLogin(results.get(0));
      } else {
        onLogout();
      }
      currentUserExists = exists;
    }
  }

  @DebugLog
  private void onLogin(User user) {
    if (listeners != null) {
      onLogout();
    }
    listeners = new ArrayList<>();

    final String userId = user.getId();

    // get and observe Room subscriptions.
    methodCall.getRoomSubscriptions().onSuccess(task -> {
      if (listeners != null) {
        Registrable listener = new StreamNotifyUserSubscriptionsChanged(
            context, hostname, realmHelper, ddpClient, userId);
        listener.register();
        listeners.add(listener);
      }
      return null;
    }).continueWith(new LogcatIfError());
  }

  @DebugLog
  private void onLogout() {
    if (listeners != null) {
      for (Registrable listener : listeners) {
        listener.unregister();
      }
    }
    listeners = null;
  }
}
