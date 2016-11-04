package chat.rocket.android.activity;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import chat.rocket.android.helper.OnBackPressListener;

abstract class AbstractFragmentActivity extends AppCompatActivity {

  protected abstract @IdRes int getLayoutContainerForFragment();

  @Override public void onBackPressed() {
    Fragment fragment =
        getSupportFragmentManager().findFragmentById(getLayoutContainerForFragment());

    if (fragment instanceof OnBackPressListener
        && ((OnBackPressListener) fragment).onBackPressed()) {
      //consumed. do nothing.
    } else {
      super.onBackPressed();
    }
  }

  protected void showFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(getLayoutContainerForFragment(), fragment)
        .commit();
  }

  protected void showFragmentWithBackStack(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(getLayoutContainerForFragment(), fragment)
        .addToBackStack(null)
        .commit();
  }
}
