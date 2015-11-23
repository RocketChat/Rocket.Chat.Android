package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

abstract class AbstractActivity extends AppCompatActivity {

    abstract protected @IdRes int getContainerId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    public void onBackPressed(){
        Fragment f = getSupportFragmentManager().findFragmentById(getContainerId());
        if(f instanceof OnBackPressListener &&
                ((OnBackPressListener) f).onBackPressed()){
            //consumed. do nothing.
        }
        else super.onBackPressed();
    }
}
