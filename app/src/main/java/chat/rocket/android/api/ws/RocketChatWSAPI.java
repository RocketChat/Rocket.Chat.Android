package chat.rocket.android.api.ws;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.OkHttpHelper;
import chat.rocket.android.model.User;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
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
        if(Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            param.put("user", new JSONObject().put("email", username));
        }
        else {
            param.put("user", new JSONObject().put("username", username));
        }
        param.put("password", new JSONObject().put("digest", hashedPassword).put("algorithm", "sha-256"));

        return mDDPClient.rpc("login", new JSONArray().put(param) ,generateId("login"));
    }

    public Task<DDPClientCallback.RPC> loginOauth(JSONObject oauth) throws JSONException {
        return mDDPClient.rpc("login", new JSONArray().put(new JSONObject().put("oauth",oauth)) ,generateId("login-oauth"));
    }

    public Task<DDPClientCallback.RPC> login(final String token) throws JSONException {
        JSONObject param = new JSONObject();
        param.put("resume", token);

        return mDDPClient.rpc("login", new JSONArray().put(param) ,generateId("login-token"));
    }

    public Task<DDPClientCallback.RPC> logout(){
        return mDDPClient.rpc("logout",null,generateId("logout"));
    }

    public Task<DDPClientCallback.RPC> sendMessage(final String roomId, String msg, @Nullable  JSONObject extraParams) throws JSONException {
        JSONObject param = new JSONObject()
                .put("_id", generateId("message-doc"))
                .put("rid", roomId)
                .put("msg", msg);
        if(extraParams!=null) {
            if(!extraParams.isNull("file")) param.put("file", extraParams.getJSONObject("file"));
            if(!extraParams.isNull("groupable")) param.put("groupable", extraParams.getBoolean("groupable"));
            if(!extraParams.isNull("attachments")) param.put("attachments", extraParams.getJSONArray("attachments"));
        }

        return mDDPClient.rpc("sendMessage", new JSONArray().put(param) ,generateId("message"));
    }

    public Task<DDPClientCallback.RPC> getRooms(long time) throws JSONException {
        JSONArray params = new JSONArray()
                .put(new JSONObject().put("$date",time));

        return mDDPClient.rpc("rooms/get", params, generateId("rooms-get"));
    }

    public Task<DDPClientCallback.RPC> loadMessages(final String roomID, long endTs, int num) throws JSONException {
        JSONArray params = new JSONArray()
                .put(roomID)
                .put(endTs > 0 ? new JSONObject().put("$date",endTs) : null)
                .put(num)
                .put(new JSONObject().put("$date",System.currentTimeMillis()));

        return mDDPClient.rpc("loadHistory", params, generateId("load-message"));
    }

    public Task<DDPClientCallback.RPC> searchMessage(final String roomId, final String query) {
        return mDDPClient.rpc("messageSearch",
                new JSONArray().put(query).put(roomId),
                generateId("search-message"));
    }

    public Task<DDPClientCallback.RPC> setUserStatus(User.Status status) {
        return mDDPClient.rpc("UserPresence:setDefaultStatus",
                new JSONArray().put(status.getValue()),
                generateId("user-status"));
    }

    public Task<DDPClientCallback.RPC> createChannel(String name, JSONArray usernames) {
        JSONArray params = new JSONArray()
                .put(name)
                .put(usernames);
        return mDDPClient.rpc("createChannel",params,generateId("create-ch"));
    }

    public Task<DDPClientCallback.RPC> createPrivateGroup(String name, JSONArray usernames) {
        JSONArray params = new JSONArray()
                .put(name)
                .put(usernames);
        return mDDPClient.rpc("createPrivateGroup",params,generateId("create-pg"));
    }

    public Task<DDPClientCallback.RPC> createDirectMessage(String username) {
        return mDDPClient.rpc("createDirectMessage",
                new JSONArray().put(username),
                generateId("create-dm"));
    }

    public interface UploadFileProgress{
        void onProgress(long sent, long total);
    }

    private static class FSCounter{
        private UploadFileProgress mListener;
        private long mCnt;
        private final long mTotal;
        public FSCounter(long initCount, long totalSize) {
            mCnt = initCount;
            mTotal = totalSize;
        }
        public FSCounter setListener(UploadFileProgress listener) {
            mListener = listener;
            return this;
        }
        public void inc(long size){
            mCnt += size;
            if(mListener!=null) mListener.onProgress(mCnt, mTotal);
        }
        public void complete(){
            mCnt = mTotal;
            if(mListener!=null) mListener.onProgress(mCnt, mTotal);
        }
    }

    public Task<DDPClientCallback.RPC> uploadFile(final Context context, final String roomID, final String userID, final String filename, final Uri fileUri, final String mimeType, final long fileSize, @Nullable final UploadFileProgress listener) throws JSONException {
        final String fileID = generateId("upl-file");
        final FSCounter cnt = new FSCounter(0, fileSize).setListener(listener);
        return prepareForUploadingFile(roomID, userID, fileID, filename, mimeType, fileSize)
                .onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<DDPClientCallback.RPC>>() {
                    @Override
                    public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.RPC> task) throws Exception {
                        InputStream s = context.getContentResolver().openInputStream(fileUri);
                        byte[] buf = new byte[8192];

                        Task<DDPClientCallback.RPC> t = Task.forResult(null);
                        while(true) {
                            final int size = s.read(buf);
                            if(size<=0) break;

                            final String chunk = Base64.encodeToString(buf,0,size,Base64.NO_WRAP);
                            t = t.onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<DDPClientCallback.RPC>>() {
                                @Override
                                public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.RPC> task) throws Exception {
                                    if(task.getResult()!=null) {
                                        cnt.inc(size);
                                    }
                                    return ufsWrite(fileID, chunk);
                                }
                            });
                        }
                        return t;
                    }
                }).onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<DDPClientCallback.RPC>>() {
                    @Override
                    public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.RPC> task) throws Exception {
                        cnt.complete();
                        return ufsComplete(fileID);
                    }
                });
    }

    private Task<DDPClientCallback.RPC> prepareForUploadingFile(final String roomID, final String userID, final String fileID, final String filename, final String mimeType, final long fileSize) throws JSONException {
        final String collectionName = "rocketchat_uploads";
        JSONObject param = new JSONObject()
                //provide document ID in advance to prevent generate random ID (that is not notified via DDP Callback...)
                // refs: Meteor:packages/mongo/collection.js: Mongo.Collection.prototype.insert
                .put("_id", fileID)

                .put("rid", roomID)
                .put("userID", userID)
                .put("name", filename)
                .put("type", mimeType)
                .put("size", fileSize)
                .put("complete", false)
                .put("uploading", true)
                .put("store", collectionName);

        return mDDPClient.rpc("/"+collectionName+"/insert", new JSONArray().put(param), generateId("upl-file-prepare"));
    }

    private Task<DDPClientCallback.RPC> ufsWrite(final String fileID, String chunk) throws JSONException {
        final String collectionName = "rocketchat_uploads";
        JSONArray params = new JSONArray()
                .put(new JSONObject().put("$binary", chunk))
                .put(fileID)
                .put(collectionName);
        return mDDPClient.rpc("ufsWrite", params, generateId("upl-file-write"));
    }

    private Task<DDPClientCallback.RPC> ufsComplete(final String fileID) throws JSONException {
        final String collectionName = "rocketchat_uploads";
        JSONArray params = new JSONArray()
                .put(fileID)
                .put(collectionName);
        return mDDPClient.rpc("ufsComplete", params, generateId("upl-file-comlete"));
    }

    public Task<DDPClientCallback.RPC> markAsRead(final String roomID) throws JSONException {
        return mDDPClient.rpc("readMessages", new JSONArray().put(roomID), generateId("read-msg"));
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
