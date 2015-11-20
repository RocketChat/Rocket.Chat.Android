package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import chat.rocket.android.R;

abstract class AbstractActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    public void onBackPressed(){
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.simple_framelayout);
        if(f instanceof OnBackPressListener &&
                ((OnBackPressListener) f).onBackPressed()){
            //consumed. do nothing.
        }
        else super.onBackPressed();
    }

    public interface OnBackPressListener{
        boolean onBackPressed();
    }
}
