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

        new RocketChatRestAPI().login("hoge@hoge.net","hogehoge").onSuccess(new Continuation<UserAuth, Object>() {
            @Override
            public Object then(Task<UserAuth> task) throws Exception {
                UserAuth auth = task.getResult();
                Log.d("hoge",auth.userId+" / "+auth.authToken);
                return null;
            }
        });
    }
}
