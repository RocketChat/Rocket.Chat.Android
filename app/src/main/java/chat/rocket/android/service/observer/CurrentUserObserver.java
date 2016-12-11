package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.Registerable;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserNotification;
import chat.rocket.android.service.ddp.stream.StreamNotifyUserSubscriptionsChanged;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;

/**
 * observe the user with emails.
 */
public class CurrentUserObserver extends AbstractModelObserver<User> {
  private boolean currentUserExists;
  private final MethodCallHelper methodCall;


  private ArrayList<Registerable> listeners;

  public CurrentUserObserver(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
    currentUserExists = false;
  }

  @Override public RealmResults<User> queryItems(Realm realm) {
    return User.queryCurrentUser(realm).findAll();
  }

  @Override public void onUpdateResults(List<User> results) {
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

    final String userId = user.get_id();

    // get and observe Room subscriptions.
    methodCall.getRoomSubscriptions().onSuccess(task -> {
      Registerable listener = new StreamNotifyUserSubscriptionsChanged(
          context, hostname, realmHelper, ddpClient, userId);
      listener.register();
      listeners.add(listener);
      return null;
    });

    Registerable listener = new StreamNotifyUserNotification(
        context, hostname, realmHelper, ddpClient, userId);
    listener.register();
    listeners.add(listener);
  }

  @DebugLog
  private void onLogout() {
    if (listeners != null) {
      for (Registerable listener : listeners) {
        listener.unregister();
      }
    }
    listeners = null;
  }
}
