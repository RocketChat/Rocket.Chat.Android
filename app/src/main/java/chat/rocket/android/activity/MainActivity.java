package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.model.UserAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RocketChatRestAPI().login("hoge@hoge.net","hogehoge").onSuccessTask(new Continuation<UserAuth, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<UserAuth> task) throws Exception {
                UserAuth auth = task.getResult();
                Log.d("hoge", auth.userId + " / " + auth.authToken);

                return new RocketChatRestAPI().logout(auth);
            }
        }).onSuccess(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {

                return null;
            }
        });
    }
}
