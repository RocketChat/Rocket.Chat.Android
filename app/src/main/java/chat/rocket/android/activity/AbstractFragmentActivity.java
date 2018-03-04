package chat.rocket.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

  // Hide Keyboard when click blank area
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      View view = getCurrentFocus();
      if (isShouldHideKeyboard(view, ev)) {
        try {
          hideSoftInput(view.getWindowToken());
        } catch (NullPointerException e) {
          throw new NullPointerException
                  ("AbstractFragmentActivity: getWindowToken() returns null");
        }
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  protected boolean isShouldHideKeyboard(View view, MotionEvent ev) {
    if (view != null && (view instanceof EditText)) {
      int[] l = {0, 0};
      view.getLocationInWindow(l);
      int left = l[0], top = l[1];
      int bottom = top + view.getHeight(), right = left + view.getWidth();
      return !(ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom);
    }
    return false;
  }

  private void hideSoftInput(IBinder token) {
    if (token != null) {
      InputMethodManager manager =
              (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      try {
        manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
      } catch (NullPointerException e) {
        throw new NullPointerException
                ("AbstractFragmentActivity: hideSoftInputFromWindow() returns null");
      }
    }
  }

}
