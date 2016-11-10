package chat.rocket.android.activity;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import chat.rocket.android.helper.OnBackPressListener;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

abstract class AbstractFragmentActivity extends RxAppCompatActivity {

  protected abstract @IdRes int getLayoutContainerForFragment();

  @Override public final void onBackPressed() {
    if (!onBackPress()) {
      onBackPresseNotHandled();
    }
  }

  protected boolean onBackPress() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(getLayoutContainerForFragment());

    if (fragment instanceof OnBackPressListener
        && ((OnBackPressListener) fragment).onBackPressed()) {
      return true;
    }

    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
      return true;
    }

    return false;
  }

  protected void onBackPresseNotHandled() {
    super.onBackPressed();
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
