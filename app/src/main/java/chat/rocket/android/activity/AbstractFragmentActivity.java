package chat.rocket.android.activity;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import chat.rocket.android.helper.OnBackPressListener;

abstract class AbstractFragmentActivity extends AppCompatActivity {

    protected abstract @IdRes int getLayoutContainerForFragment();

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(getLayoutContainerForFragment());
        if (f instanceof OnBackPressListener && ((OnBackPressListener) f).onBackPressed()) {
            //consumed. do nothing.
        } else super.onBackPressed();
    }

    protected void showFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(getLayoutContainerForFragment(), f)
                .commit();
    }

    protected void showFragmentWithBackStack(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(getLayoutContainerForFragment(), f)
                .addToBackStack(null)
                .commit();
    }
}
