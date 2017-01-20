package chat.rocket.android.service;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

/**
 * Backend implementation to store ServerInfo.
 */
public class ServerInfoImpl extends RealmObject {
  private static final String DB_NAME = "serverlist";

  @PrimaryKey private String hostname;
  private String name;
  private String session;
  private boolean insecure;

  interface ColumnName {
    String HOSTNAME = "hostname";
    String NAME = "name";
    String SESSION = "session";
    String INSECURE = "insecure";
  }

  ServerInfo getServerInfo() {
    return new ServerInfo(hostname, name, session, insecure);
  }

  static RealmHelper getRealm() {
    return RealmStore.getOrCreate(DB_NAME);
  }

  static void addOrUpdate(String hostname, String name) {
    getRealm().executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerInfoImpl.class, new JSONObject()
            .put(ColumnName.HOSTNAME, hostname)
            .put(ColumnName.NAME, TextUtils.isEmpty(name) ? JSONObject.NULL : name)));
  }

  static void remove(String hostname) {
    getRealm().executeTransaction(realm -> {
      realm.where(ServerInfoImpl.class).equalTo(ColumnName.HOSTNAME, hostname)
          .findAll()
          .deleteAllFromRealm();
      return null;
    });
  }

  static void updateSession(String hostname, String session) {
    ServerInfoImpl impl = getRealm().executeTransactionForRead(realm ->
        realm.where(ServerInfoImpl.class).equalTo(ColumnName.HOSTNAME, hostname).findFirst());

    if (impl != null) {
      impl.session = session;
      getRealm().executeTransaction(realm -> {
        realm.copyToRealmOrUpdate(impl);
        return null;
      });
    }
  }

  static @Nullable ServerInfo getServerInfoForHost(String hostname) {
    ServerInfoImpl impl = getRealm().executeTransactionForRead(realm ->
        realm.where(ServerInfoImpl.class).equalTo(ColumnName.HOSTNAME, hostname).findFirst());
    return impl == null ? null : impl.getServerInfo();
  }

  static void setInsecure(String hostname, boolean insecure) {
    ServerInfoImpl impl = getRealm().executeTransactionForRead(realm ->
        realm.where(ServerInfoImpl.class).equalTo(ColumnName.HOSTNAME, hostname).findFirst());

    if (impl != null) {
      impl.insecure = insecure;
      getRealm().executeTransaction(realm -> {
        realm.copyToRealmOrUpdate(impl);
        return null;
      });
    }
  }

  static List<ServerInfo> getAllFromRealm() {
    List<ServerInfoImpl> results = getRealm().executeTransactionForReadResults(realm ->
        realm.where(ServerInfoImpl.class).findAll());
    ArrayList<ServerInfo> list = new ArrayList<>();
    for (ServerInfoImpl impl : results) {
      list.add(impl.getServerInfo());
    }
    return list;
  }
}
