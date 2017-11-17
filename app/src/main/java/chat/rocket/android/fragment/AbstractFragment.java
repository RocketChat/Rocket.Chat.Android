package chat.rocket.android.fragment;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle2.components.support.RxFragment;

/**
 * Fragment base class for this Application.
 */
public abstract class AbstractFragment extends RxFragment {
  protected View rootView;

  @LayoutRes
  protected abstract int getLayout();

  protected abstract void onSetupView();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    rootView = inflater.inflate(getLayout(), container, false);
    onSetupView();
    return rootView;
  }

  protected void finish() {
    if (getFragmentManager().getBackStackEntryCount() == 0) {
      getActivity().finish();
    } else {
      getFragmentManager().popBackStack();
    }
  }
}
