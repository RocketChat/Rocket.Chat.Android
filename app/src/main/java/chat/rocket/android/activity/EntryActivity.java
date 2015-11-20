package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import chat.rocket.android.R;
import chat.rocket.android.SplashFragment;

public class EntryActivity extends AbstractActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simple_framelayout);

        findViewById(R.id.simple_framelayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new SplashFragment())
                .commit();
    }
}
