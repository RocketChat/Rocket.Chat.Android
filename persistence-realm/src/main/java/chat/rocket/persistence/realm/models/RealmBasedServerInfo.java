package chat.rocket.persistence.realm.models;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;

/**
 * Backend implementation to store ServerInfo.
 */
public class RealmBasedServerInfo extends RealmObject {
  private static final String DB_NAME = "server.list";

  @PrimaryKey private String hostname;
  private String name;
  private String session;
  private boolean insecure;

  public interface ColumnName {
    String HOSTNAME = "hostname";
    String NAME = "name";
    String SESSION = "session";
    String INSECURE = "insecure";
  }

  public ServerInfo getServerInfo() {
    return ServerInfo.builder()
        .setHostname(hostname)
        .setName(name)
        .setSession(session)
        .setSecure(!insecure)
        .build();
  }

  public static Realm getRealm() {
    return RealmStore.getRealm(DB_NAME);
  }

  public static RealmHelper getRealmHelper() {
    return RealmStore.getOrCreateForServerScope(DB_NAME);
  }

  public static void addOrUpdate(String hostname, String name, boolean insecure) {
    getRealmHelper().executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(RealmBasedServerInfo.class, new JSONObject()
            .put(ColumnName.HOSTNAME, hostname)
            .put(ColumnName.NAME, TextUtils.isEmpty(name) ? JSONObject.NULL : name)
            .put(ColumnName.INSECURE, insecure)));
  }

  public static void remove(String hostname) {
    getRealmHelper().executeTransaction(realm -> {
      realm.where(RealmBasedServerInfo.class).equalTo(ColumnName.HOSTNAME, hostname)
          .findAll()
          .deleteAllFromRealm();
      return null;
    });
  }

  public static void updateSession(String hostname, String session) {
    RealmBasedServerInfo impl = getRealmHelper().executeTransactionForRead(realm ->
        realm.where(RealmBasedServerInfo.class).equalTo(ColumnName.HOSTNAME, hostname).findFirst());

    if (impl != null) {
      impl.session = session;
      getRealmHelper().executeTransaction(realm -> {
        realm.copyToRealmOrUpdate(impl);
        return null;
      });
    }
  }

  @Nullable
  public static ServerInfo getServerInfoForHost(String hostname) {
    RealmBasedServerInfo impl = getRealmHelper().executeTransactionForRead(realm ->
        realm.where(RealmBasedServerInfo.class).equalTo(ColumnName.HOSTNAME, hostname).findFirst());
    return impl == null ? null : impl.getServerInfo();
  }

  public static List<ServerInfo> getServerInfoList() {
    List<RealmBasedServerInfo> results = getRealmHelper().executeTransactionForReadResults(realm ->
        realm.where(RealmBasedServerInfo.class).findAll());
    ArrayList<ServerInfo> list = new ArrayList<>();
    for (RealmBasedServerInfo impl : results) {
      list.add(impl.getServerInfo());
    }
    return list;
  }
}
