package chat.rocket.android.api.rest;

import android.content.Context;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class DefaultCookieProvider implements CookieProvider {

  private final Context applicationContext;

  public DefaultCookieProvider(Context context) {
    applicationContext = context.getApplicationContext();
  }

  @Override
  public String getHostname() {
    return getHostnameFromCache();
  }

  @Override
  public String getCookie() {
    final String hostname = getHostnameFromCache();
    if (hostname == null) {
      return "";
    }

    final RealmHelper realmHelper = RealmStore.get(getHostnameFromCache());
    if (realmHelper == null) {
      return "";
    }

    final String userId = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst()).getId();
    final String token = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst()).getToken();

    return "rc_uid=" + userId + ";rc_token=" + token;
  }

  private String getHostnameFromCache() {
    return RocketChatCache.getSelectedServerHostname(applicationContext);
  }
}
