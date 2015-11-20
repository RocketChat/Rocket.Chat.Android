package chat.rocket.android.activity;

import android.os.Bundle;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.model.Auth;

public class MainActivity extends AbstractActivity {
    private static final String TAG = Constants.LOG_TAG;

    Auth mUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_pane);
    }
}
