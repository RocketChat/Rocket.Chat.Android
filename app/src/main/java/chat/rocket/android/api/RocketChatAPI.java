package chat.rocket.android.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import bolts.Continuation;
import bolts.Task;
import jp.co.crowdworks.android_meteor.ddp.DDPClient;
import jp.co.crowdworks.android_meteor.ddp.DDPClientCallback;

public class RocketChatAPI {
    private final DDPClient mDDPClient;
    private final String mHostName;

    private RocketChatAPI(){
        this("demo.rocket.chat");
    }
    public RocketChatAPI(String hostname){
        mDDPClient = new DDPClient(OkHttpHelper.getClient());
        mHostName = hostname;
    }

    private static String sha256sum(String orig) {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        d.update(orig.getBytes());

        StringBuilder sb = new StringBuilder();
        for(byte b : d.digest()) sb.append(String.format("%02x", b & 0xff));

        return sb.toString();
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

    public Task<DDPClientCallback.RPC> login(final String username, final String password) throws JSONException {
        JSONObject param = new JSONObject();
        param.put("user", new JSONObject().put("email", username));
        param.put("password", new JSONObject().put("digest", sha256sum(password)).put("algorithm", "sha-256"));

        return mDDPClient.rpc("login", new JSONArray().put(param) ,generateId("login"));
    }

    public Task<DDPClientCallback.RPC> logout(){
        return mDDPClient.rpc("logout",null,generateId("logout"));
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
