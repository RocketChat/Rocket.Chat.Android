package chat.rocket.android.api.ws;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.OkHttpHelper;
import jp.co.crowdworks.android_meteor.ddp.DDPClient;
import jp.co.crowdworks.android_meteor.ddp.DDPClientCallback;

/*package*/ class RocketChatWSAPI {
    private final DDPClient mDDPClient;
    private final String mHostName;

    private RocketChatWSAPI(){
        this("demo.rocket.chat");
    }
    public RocketChatWSAPI(String hostname){
        mDDPClient = new DDPClient(OkHttpHelper.getClient());
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
        param.put("user", new JSONObject().put("resume", token));

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

    public void trial(final String username, final String password){
        connect().onSuccessTask(new Continuation<DDPClientCallback.Connect, Task<DDPClientCallback.RPC>>() {
            @Override
            public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.Connect> task) throws Exception {
                return login(username, password);
            }
        }).onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<DDPClientCallback.RPC>>() {
            @Override
            public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.RPC> task) throws Exception {
                return sendMessage("GENERAL", "Hello World! time="+System.currentTimeMillis());
            }
        }).onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<DDPClientCallback.RPC>>() {
            @Override
            public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.RPC> task) throws Exception {
                return logout();
            }
        }).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
            @Override
            public Object then(Task<DDPClientCallback.RPC> task) throws Exception {

                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if(task.isFaulted()){
                    Log.e("hoge","error",task.getError());
                }
                return null;
            }
        });
    }
}
