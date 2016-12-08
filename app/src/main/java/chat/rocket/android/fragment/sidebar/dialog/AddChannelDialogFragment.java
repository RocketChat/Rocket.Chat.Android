package chat.rocket.android.fragment.sidebar.dialog;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

/**
 * add Channel, add Private-group.
 */
public class AddChannelDialogFragment extends AbstractAddRoomDialogFragment {
  public AddChannelDialogFragment() {}

  @Override protected int getLayout() {
    return R.layout.dialog_add_channel;
  }

  @Override protected void onSetupDialog() {
    View buttonAddChannel = getDialog().findViewById(R.id.btn_add_channel);

    RxTextView.textChanges((TextView) getDialog().findViewById(R.id.editor_channel_name))
        .map(text -> !TextUtils.isEmpty(text))
        .compose(bindToLifecycle())
        .subscribe(RxView.enabled(buttonAddChannel));

    buttonAddChannel.setOnClickListener(view -> {
      TextView channelNameText = (TextView) getDialog().findViewById(R.id.editor_channel_name);
      String channelName = channelNameText.getText().toString();
      boolean isPrivate = isChecked(R.id.checkbox_private);
      boolean isReadOnly = isChecked(R.id.checkbox_read_only);
      createChannel(channelName, isPrivate, isReadOnly);
    });
  }

  private boolean isChecked(int viewId) {
    CompoundButton check = (CompoundButton) getDialog().findViewById(viewId);
    return check.isChecked();
  }

  private void createChannel(String name, boolean isPrivate, boolean isReadOnly) {

  }
}
