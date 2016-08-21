package chat.rocket.android.model2;

import org.json.JSONObject;

import java.util.UUID;

import chat.rocket.android.Constants;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

public class MethodCall extends RealmObject {
    @PrimaryKey
    private String id;
    private long timestamp;
    private int syncstate;
    private String op;
    private String params;
    private String returns;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSyncstate() {
        return syncstate;
    }

    public void setSyncstate(int syncstate) {
        this.syncstate = syncstate;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getReturns() {
        return returns;
    }

    public void setReturns(String returns) {
        this.returns = returns;
    }

    public static String create(final String op, final JSONObject params) {
        final String id = UUID.randomUUID().toString();
        RealmHelperBolts.executeTransactionAsync(new RealmHelperBolts.Transaction() {
            @Override
            public Object execute(Realm realm) throws Exception {
                realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                        .put("id", id)
                        .put("op", op)
                        .put("syncstate", SyncState.NOT_SYNCED)
                        .put("params", params!=null ? params.toString() : "{}")
                        .put("timestamp", System.currentTimeMillis())
                );
                return null;
            }
        }).continueWith(Constants.ERROR_LOGGING);

        return id;
    }

    public static void delete(final String id) {
        RealmHelperBolts.executeTransactionAsync(new RealmHelperBolts.Transaction() {
            @Override
            public Object execute(Realm realm) throws Exception {
                realm.where(MethodCall.class).equalTo("id", id).findAll().deleteAllFromRealm();
                return null;
            }
        }).continueWith(Constants.ERROR_LOGGING);
    }
}
