package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.UserAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = Constants.LOG_TAG;

    UserAuth mUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RocketChatRestAPI rocket = new RocketChatRestAPI("demorocket.herokuapp.com");
        rocket.login("hoge@hoge.net", "hogehoge").onSuccessTask(new Continuation<UserAuth, Task<List<Room>>>() {
            @Override
            public Task<List<Room>> then(Task<UserAuth> task) throws Exception {
                mUserAuth = task.getResult();
                Log.d(TAG, mUserAuth.userId + " / " + mUserAuth.authToken);

                return rocket.getPublicRooms(mUserAuth);
            }
        }).onSuccessTask(new Continuation<List<Room>, Task<List<Message>>>() {
            @Override
            public Task<List<Message>> then(Task<List<Room>> task) throws Exception {
                List<Room> rooms = task.getResult();
                for (Room room : rooms) {
                    Log.d(TAG, room.id + "/" + room.name);
                }
                return rocket.listRecentMessages(mUserAuth, rooms.get(0));
            }
        }).onSuccess(new Continuation<List<Message>, Object>() {
            @Override
            public Object then(Task<List<Message>> task) throws Exception {
                List<Message> messages = task.getResult();
                for (Message m : messages) {
                    Log.d(TAG, m.id + "/"+ m.user.name +"["+m.user.id+"]: " + m.content+"\t"+m.timestamp);
                }
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e(TAG, "error", task.getError());
                }
                return null;
            }
        });
    }
}
