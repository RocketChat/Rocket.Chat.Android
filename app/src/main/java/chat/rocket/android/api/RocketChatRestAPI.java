package chat.rocket.android.api;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.model.UserAuth;

public class RocketChatRestAPI {
    private static final String HOSTNAME = "demorocket.herokuapp.com"; //TODO: user-configurable!

    public RocketChatRestAPI(){}

    private HttpUrl.Builder baseURL(){
        return new HttpUrl.Builder()
                .scheme("https")
                .host(HOSTNAME)
                .addPathSegment("api");
    }

    public interface ResponseParser<T> {
        T parse(JSONObject data) throws JSONException;
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
                        task.setResult(parser.parse(json.getJSONObject("data")));
                    }
                    else task.setError(new Exception(json.getJSONObject("data").getString("message")));
                }
                catch (JSONException e) {
                    task.setError(e);
                }
            }
        });
        return task.getTask();
    }

    public Task<UserAuth> login(String username, String password){
        RequestBody formBody = new FormEncodingBuilder()
                .add("user", username)
                .add("password", password)
                .build();

        return baseRequest(
                new Request.Builder()
                    .post(formBody)
                    .url(baseURL().addPathSegment("login").build())
                    .build(),
                new ResponseParser<UserAuth>() {
                    @Override
                    public UserAuth parse(JSONObject data) throws JSONException {
                        UserAuth userAuth = new UserAuth();
                        userAuth.userId = data.getString("userId");
                        userAuth.authToken = data.getString("authToken");
                        return userAuth;
                    }
                });
    }
}
