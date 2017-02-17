package chat.rocket.android.fragment.sidebar.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.jakewharton.rxbinding.widget.RxTextView;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

/**
 * add Channel, add Private-group.
 */
public class AddChannelDialogFragment extends AbstractAddRoomDialogFragment {
  public AddChannelDialogFragment() {
  }

  public static AddChannelDialogFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);

    AddChannelDialogFragment fragment = new AddChannelDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_channel;
  }

  @Override
  protected void onSetupDialog() {
    View buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);

    RxJavaInterop.toV2Flowable(
        RxTextView.textChanges((TextView) getDialog().findViewById(R.id.editor_channel_name)))
        .map(text -> !TextUtils.isEmpty(text))
        .compose(bindToLifecycle())
        .subscribe(buttonAddChannel::setEnabled);

    buttonAddChannel.setOnClickListener(view -> createRoom());
  }

  private boolean isChecked(int viewId) {
    CompoundButton check = (CompoundButton) getDialog().findViewById(viewId);
    return check.isChecked();
  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    TextView channelNameText = (TextView) getDialog().findViewById(R.id.editor_channel_name);
    String channelName = channelNameText.getText().toString();
    boolean isPrivate = isChecked(R.id.checkbox_private);
    boolean isReadOnly = isChecked(R.id.checkbox_read_only);

    if (isPrivate) {
      return methodCall.createPrivateGroup(channelName, isReadOnly);
    } else {
      return methodCall.createChannel(channelName, isReadOnly);
    }
  }
}
