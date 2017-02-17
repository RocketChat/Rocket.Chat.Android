package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.wrappers.InstabugWrapper;
import icepick.Icepick;

abstract class AbstractFragmentActivity extends RxAppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Icepick.restoreInstanceState(this, savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Icepick.saveInstanceState(this, outState);
  }

  protected abstract
  @IdRes
  int getLayoutContainerForFragment();

  @Override
  public final void onBackPressed() {
    if (!onBackPress()) {
      onBackPressedNotHandled();
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

  protected void showFragmentWithBackStack(Fragment fragment) {
    getSupportFragmentManager().beginTransaction()
        .replace(getLayoutContainerForFragment(), fragment)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    try {
      InstabugWrapper.trackTouchEventOnActivity(event, this);
    } catch (IllegalStateException exception) {
      RCLog.w(exception, "Instabug error (ignored)");
    }
    return super.dispatchTouchEvent(event);
  }
}
