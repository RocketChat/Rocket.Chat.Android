package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;

import chat.rocket.android.R;
import chat.rocket.android.fragment.SplashFragment;

public class EntryActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simple_framelayout);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new SplashFragment())
                .commit();
    }
}
