package chat.rocket.android.model;

import org.json.JSONObject;

import chat.rocket.android.helper.LogcatIfError;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 * Server configuration
 */
@SuppressWarnings("PMD.ShortVariable")
public class ServerConfig extends RealmObject {
    @PrimaryKey
    private String id;
    private String hostname;
    private String connectionError;
    private String token;
    private boolean tokenVerified;
    private RealmList<MeteorLoginServiceConfiguration> authProviders;
    private String selectedProviderName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getConnectionError() {
        return connectionError;
    }

    public void setConnectionError(String connectionError) {
        this.connectionError = connectionError;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTokenVerified() {
        return tokenVerified;
    }

    public void setTokenVerified(boolean tokenVerified) {
        this.tokenVerified = tokenVerified;
    }

    public RealmList<MeteorLoginServiceConfiguration> getAuthProviders() {
        return authProviders;
    }

    public void setAuthProviders(RealmList<MeteorLoginServiceConfiguration> authProviders) {
        this.authProviders = authProviders;
    }

    public String getSelectedProviderName() {
        return selectedProviderName;
    }

    public void setSelectedProviderName(String selectedProviderName) {
        this.selectedProviderName = selectedProviderName;
    }

    public static RealmQuery<ServerConfig> queryLoginRequiredConnections(Realm realm) {
        return realm.where(ServerConfig.class)
                .equalTo("tokenVerified", false);
    }

    public static RealmQuery<ServerConfig> queryActiveConnections(Realm realm) {
        return realm.where(ServerConfig.class)
                .isNotNull("token");
    }

    public static boolean hasActiveConnection() {
        ServerConfig config = RealmHelper.executeTransactionForRead(realm ->
                queryActiveConnections(realm).findFirst());

        return config != null;
    }

    @DebugLog
    public static void logError(String id, Exception exception) {
        RealmHelperBolts
                .executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
                                .put("id", id)
                                .put("connectionError", exception.getMessage())))
                .continueWith(new LogcatIfError());
    }
}
