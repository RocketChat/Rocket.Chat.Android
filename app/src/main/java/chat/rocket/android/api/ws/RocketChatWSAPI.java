package chat.rocket.android.api.ws;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.android.api.OkHttpHelper;
import jp.co.crowdworks.android_ddp.ddp.DDPClient;
import jp.co.crowdworks.android_ddp.ddp.DDPClientCallback;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.Observable;

public class RocketChatWSAPI {
    private final DDPClient mDDPClient;
    private final String mHostName;

    private RocketChatWSAPI(){
        this("demo.rocket.chat");
    }
    public RocketChatWSAPI(String hostname){
        mDDPClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
        mHostName = hostname;
    }

    private static class Counter1000{
        private int n=0;
        public int next(){
            n++;
            n%=1000;
            return n;
        }
    };
    private static Counter1000 sCounter = new Counter1000();

    private String generateId(String method) {
        return method+Long.toString(System.currentTimeMillis())+sCounter.next();
    }

    public Task<DDPClientCallback.Connect> connect() {
        return mDDPClient.connect("wss://"+mHostName+"/websocket");
    }

    public Task<DDPClientCallback.RPC> login(final String username, final String hashedPassword) throws JSONException {
        JSONObject param = new JSONObject();
        param.put("user", new JSONObject().put("email", username));
        param.put("password", new JSONObject().put("digest", hashedPassword).put("algorithm", "sha-256"));

        return mDDPClient.rpc("login", new JSONArray().put(param) ,generateId("login"));
    }

    public Task<DDPClientCallback.RPC> login(final String token) throws JSONException {
        JSONObject param = new JSONObject();
        param.put("resume", token);

        return mDDPClient.rpc("login", new JSONArray().put(param) ,generateId("login-token"));
    }

    public Task<DDPClientCallback.RPC> logout(){
        return mDDPClient.rpc("logout",null,generateId("logout"));
    }

    public Task<DDPClientCallback.RPC> sendMessage(final String roomId, String msg) throws JSONException {
        JSONObject param = new JSONObject()
                .put("_id", generateId("message-doc"))
                .put("rid", roomId)
                .put("msg", msg);

        return mDDPClient.rpc("sendMessage", new JSONArray().put(param) ,generateId("message"));
    }

    public Task<DDPClientCallback.RPC> loadMessages(final String roomID, long endTs, int num) throws JSONException {
        JSONArray params = new JSONArray()
                .put(roomID)
                .put(endTs > 0 ? new JSONObject().put("$date",endTs) : null)
                .put(num)
                .put(new JSONObject().put("$date",System.currentTimeMillis()));

        return mDDPClient.rpc("loadHistory", params, generateId("load-message"));
    }

    public Task<DDPSubscription.Ready> subscribe(final String name, JSONArray param) {
        return mDDPClient.sub(generateId("sub"), name, param);
    }

    public Task<DDPSubscription.NoSub> unsubscribe(final String id) {
        return mDDPClient.unsub(id);
    }

    public Observable<DDPSubscription.Event> getSubscriptionCallback(){
        return mDDPClient.getSubscriptionCallback();
    }

    public Observable<Void> getFailureObservable(){
        return mDDPClient.getFailureObservable();
    }

}
