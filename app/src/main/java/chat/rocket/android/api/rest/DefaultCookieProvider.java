package chat.rocket.android.api.rest;

import android.content.Context;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.model.ddp.RealmUser;
import chat.rocket.android.model.internal.Session;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;

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

    final RealmUser user = realmHelper.executeTransactionForRead(realm ->
        RealmUser.queryCurrentUser(realm).findFirst());
    final Session session = realmHelper.executeTransactionForRead(realm ->
        Session.queryDefaultSession(realm).findFirst());

    if (user == null || session == null) {
      return "";
    }

    return "rc_uid=" + user.getId() + ";rc_token=" + session.getToken();
  }

  private String getHostnameFromCache() {
    return RocketChatCache.getSelectedServerHostname(applicationContext);
  }
}
