package chat.rocket.android.fragment.chatroom.dialog;

import android.support.v7.widget.RecyclerView;

import chat.rocket.android.R;
import chat.rocket.android.layouthelper.chatroom.dialog.MessageSelectionAdapter;
import chat.rocket.android.message.MessageSpec;

public class MessageSelectionDialogFragment extends AbstractChatRoomDialogFragment {

  public static final String TAG = "MessageSelectionDialogFragment";

  private MessageSelectionAdapter adapter;
  private ClickListener listener;

  public static MessageSelectionDialogFragment create() {
    return new MessageSelectionDialogFragment();
  }

  public MessageSelectionDialogFragment() {
    adapter = new MessageSelectionAdapter();
    adapter.setListener(messageSpec -> {
      if (listener != null) {
        listener.onClick(messageSpec);
      }
      dismiss();
    });
  }

  public void addMessageSpec(MessageSpec messageSpec) {
    adapter.addMessageSpec(messageSpec);
  }

  public void setListener(ClickListener listener) {
    this.listener = listener;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_message_selection;
  }

  @Override
  protected void onSetupDialog() {
    RecyclerView messageSpecList = (RecyclerView) getDialog().findViewById(R.id.message_spec_list);
    messageSpecList.setAdapter(adapter);
  }

  public interface ClickListener {
    void onClick(MessageSpec messageSpec);
  }
}
