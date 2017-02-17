package chat.rocket.android.fragment.sidebar.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.trello.rxlifecycle2.components.support.RxAppCompatDialogFragment;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;

public abstract class AbstractAddRoomDialogFragment extends RxAppCompatDialogFragment {

  protected RealmHelper realmHelper;
  protected MethodCallHelper methodCall;
  protected String hostname;

  protected @LayoutRes abstract int getLayout();

  protected abstract void onSetupDialog();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null) {
      handleArgs(args);
    }
  }

  protected void handleArgs(@NonNull Bundle args) {
    hostname = args.getString("hostname");
    realmHelper = RealmStore.get(hostname);
    methodCall = new MethodCallHelper(getContext(), hostname);
  }

  @Override
  public final void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    dialog.setContentView(getLayout());
    onSetupDialog();
  }

  protected final void showOrHideWaitingView(boolean show) {
    View waiting = getDialog().findViewById(R.id.waiting);
    if (waiting != null) {
      waiting.setVisibility(show ? View.VISIBLE : View.GONE);
    }
  }

  protected abstract Task<Void> getMethodCallForSubmitAction();

  protected final void createRoom() {
    showOrHideWaitingView(true);

    getMethodCallForSubmitAction().continueWith(task -> {
      showOrHideWaitingView(false);
      if (task.isFaulted()) {
        Toast.makeText(getContext(), task.getError().getMessage(), Toast.LENGTH_SHORT).show();
      } else {
        dismiss();
      }
      return null;
    });
  }
}
