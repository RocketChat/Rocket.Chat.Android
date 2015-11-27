package chat.rocket.android.api.rest;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.api.OkHttpHelper;

public class RocketChatRestAPI {
    public static class AuthorizationRequired extends Exception {
        public AuthorizationRequired(){
            super();
        }
        public AuthorizationRequired(String message){
            super(message);
        }
    }

    private final String mHostName;

    private RocketChatRestAPI(){
        this("demo.rocket.chat");
    }
    public RocketChatRestAPI(String hostname) {
        mHostName = hostname;
    }

    private HttpUrl.Builder baseURL(){
        return new HttpUrl.Builder()
                .scheme("https")
                .host(mHostName)
                .addPathSegment("api");
    }

    public interface ResponseParser<T> {
        T parse(JSONObject result) throws JSONException;
    }

    private Request.Builder createAuthRequestBuilder(Auth auth){
        return new Request.Builder()
                .header("X-User-Id", auth.userId)
                .header("X-Auth-Token", auth.authToken);
    }

    private <T> Task<T> baseRequest(final Request request, final ResponseParser<T> parser) {
        final TaskCompletionSource<T> task = new TaskCompletionSource<>();

        OkHttpHelper.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                task.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try{
                    JSONObject json = new JSONObject(response.body().string());
                    if("success".equals(json.getString("status"))) {
                        task.setResult(parser.parse(json));
                    }
                    else if(response.code()==401) task.setError(new AuthorizationRequired(json.getString("message")));
                    else task.setError(new Exception(json.getString("message")));
                }
                catch (JSONException e) {
                    task.setError(e);
                }
            }
        });
        return task.getTask();
    }

        public Task<Auth> login(String username, String password){
        RequestBody formBody = new FormEncodingBuilder()
                .add("user", username)
                .add("password", password)
                .build();

        return baseRequest(
                new Request.Builder()
                        .post(formBody)
                        .url(baseURL().addPathSegment("login").build())
                        .build(),
                new ResponseParser<Auth>() {
                    @Override
                    public Auth parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        return new Auth(data.getString("userId"), data.getString("authToken"));
                    }
                });
    }

    public Task<Boolean> logout(Auth auth) {
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("logout").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        return true;
                    }
                });

    }

    public Task<JSONArray> getPublicRooms(Auth auth) {
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("publicRooms").build())
                        .build(),
                new ResponseParser<JSONArray>() {
                    public JSONArray parse(JSONObject result) throws JSONException {
                        return result.getJSONArray("rooms");
                    }
                });
    }

    public Task<JSONArray> listRecentMessages(Auth auth, String roomId) {
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(roomId).addPathSegment("messages").build())
                        .build(),
                new ResponseParser<JSONArray>() {
                    public JSONArray parse(JSONObject result) throws JSONException {
                        return result.getJSONArray("messages");
                    }
                });

    }

    public Task<Boolean> joinToRoom(Auth auth, String roomId) {
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(roomId).addPathSegment("join").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        //
                        return true;
                    }
                });

    }

    public Task<Boolean> leaveFromRoom(Auth auth, String roomId) {
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(roomId).addPathSegment("leave").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        //
                        return true;
                    }
                });

    }

    public Task<Boolean> sendMessage(Auth auth, String roomId, String msg) {
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("msg", msg);
        } catch (JSONException e) {
            TaskCompletionSource<Boolean> task = new TaskCompletionSource<>();
            task.setError(e);
            return task.getTask();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyJson.toString());
        return baseRequest(
                createAuthRequestBuilder(auth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(roomId).addPathSegment("send").build())
                        .post(body)
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        //
                        return true;
                    }
                });

    }
}
