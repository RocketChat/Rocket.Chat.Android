package chat.rocket.android.api;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.User;
import chat.rocket.android.model.UserAuth;

public class RocketChatRestAPI {
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

    private Request.Builder createAuthRequestBuilder(UserAuth userAuth){
        return new Request.Builder()
                .header("X-User-Id", userAuth.userId)
                .header("X-Auth-Token", userAuth.authToken);
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
                    else task.setError(new Exception(json.getString("message")));
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
                    public UserAuth parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        UserAuth userAuth = new UserAuth();
                        userAuth.userId = data.getString("userId");
                        userAuth.authToken = data.getString("authToken");
                        return userAuth;
                    }
                });
    }

    public Task<Boolean> logout(UserAuth userAuth) {
        return baseRequest(
                createAuthRequestBuilder(userAuth)
                        .url(baseURL().addPathSegment("logout").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        return true;
                    }
                });

    }

    public Task<List<Room>> getPublicRooms(UserAuth userAuth) {
        return baseRequest(
                createAuthRequestBuilder(userAuth)
                        .url(baseURL().addPathSegment("publicRooms").build())
                        .build(),
                new ResponseParser<List<Room>>() {
                    public List<Room> parse(JSONObject result) throws JSONException {
                        JSONArray rooms = result.getJSONArray("rooms");

                        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                        ArrayList<Room> roomList = new ArrayList<Room>();
                        for (int i=0; i<rooms.length(); i++) {
                            JSONObject room = rooms.getJSONObject(i);

                            Room r = new Room();
                            r.id = room.getString("_id");
                            r.name = room.getString("name");
                            r.timestamp = dtf.parseDateTime(room.getString("ts"));
                            r.usernames = new ArrayList<String>();
                            JSONArray usernames = room.getJSONArray("usernames");
                            for (int j=0; j<usernames.length(); j++) {
                                r.usernames.add(j, usernames.getString(j));
                            }
                            roomList.add(r);
                        }
                        return roomList;
                    }
                });
    }

    public Task<List<Message>> listRecentMessages(UserAuth userAuth, Room room) {
        return baseRequest(
                createAuthRequestBuilder(userAuth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(room.id).addPathSegment("messages").build())
                        .build(),
                new ResponseParser<List<Message>>() {
                    public List<Message> parse(JSONObject result) throws JSONException {
                        JSONArray rooms = result.getJSONArray("messages");

                        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                        ArrayList<Message> messageList = new ArrayList<>();
                        for (int i = 0; i < rooms.length(); i++) {
                            JSONObject message = rooms.getJSONObject(i);

                            Message m = new Message();
                            m.id = message.getString("_id");
                            m.content = message.getString("msg");
                            m.timestamp = dtf.parseDateTime(message.getString("ts"));
                            m.user = new User();
                            JSONObject user = message.getJSONObject("u");
                            m.user.id = user.getString("_id");
                            m.user.name = user.getString("username");
                            messageList.add(m);
                        }
                        return messageList;
                    }
                });

    }

    public Task<Boolean> joinToRoom(UserAuth userAuth, Room room) {
        return baseRequest(
                createAuthRequestBuilder(userAuth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(room.id).addPathSegment("join").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        //
                        return true;
                    }
                });

    }

    public Task<Boolean> leaveFromRoom(UserAuth userAuth, Room room) {
        return baseRequest(
                createAuthRequestBuilder(userAuth)
                        .url(baseURL().addPathSegment("rooms").addPathSegment(room.id).addPathSegment("leave").build())
                        .build(),
                new ResponseParser<Boolean>() {
                    public Boolean parse(JSONObject result) throws JSONException {
                        JSONObject data = result.getJSONObject("data");
                        //
                        return true;
                    }
                });

    }


}
