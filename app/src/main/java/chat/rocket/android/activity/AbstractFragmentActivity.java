package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import chat.rocket.android.helper.OnBackPressListener;
import icepick.Icepick;

abstract class AbstractFragmentActivity extends RxAppCompatActivity {

  public static final String EXTRA_FINISH_ON_BACK_PRESS = "EXTRA_FINISH_ON_BACK_PRESS";
  private boolean finishOnBackPress;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    if (intent != null) {
      finishOnBackPress = intent.getBooleanExtra(EXTRA_FINISH_ON_BACK_PRESS, false);
    }
    Icepick.restoreInstanceState(this, savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  @IdRes
  protected abstract int getLayoutContainerForFragment();

  @Override
  public final void onBackPressed() {
    if (finishOnBackPress) {
      super.onBackPressed();
      finish();
    } else {
      if (!onBackPress()) {
        onBackPressedNotHandled();
      }
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

  protected void onBackPressedNotHandled() {
    super.onBackPressed();
  }

  protected void showFragment(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(getLayoutContainerForFragment(), fragment)
        .commit();
  }

  protected void showFragmentWithTagWithBackStack(Fragment fragment, String tag) {
    getSupportFragmentManager().beginTransaction()
            .replace(getLayoutContainerForFragment(), fragment, tag)
            .addToBackStack(null)
            .commit();
  }

  protected void showFragmentWithTag(Fragment fragment, String tag) {
    getSupportFragmentManager().beginTransaction()
            .replace(getLayoutContainerForFragment(), fragment, tag)
            .commit();
  }

  protected void showFragmentWithBackStack(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(getLayoutContainerForFragment(), fragment)
        .addToBackStack(null)
        .commit();
  }

  @Nullable
  protected Fragment findFragmentByTag(String tag) {
    return getSupportFragmentManager().findFragmentByTag(tag);
  }
}
