package chat.rocket.android.widget.internal;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.layouthelper.MessageExtraActionListAdapter;
import chat.rocket.android.widget.message.MessageExtraActionItemPresenter;

public class ExtraActionPickerDialogFragment extends BottomSheetDialogFragment {

  private List<MessageExtraActionItemPresenter> actionItems;
  private Callback callback;

  public static ExtraActionPickerDialogFragment create(
      List<MessageExtraActionItemPresenter> actionItems, Callback callback) {
    ExtraActionPickerDialogFragment fragment = new ExtraActionPickerDialogFragment();
    fragment.setActionItems(actionItems);
    fragment.setCallback(callback);

    return fragment;
  }

  public void setActionItems(List<MessageExtraActionItemPresenter> actionItems) {
    this.actionItems = actionItems;
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override
  public final void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    dialog.setContentView(R.layout.dialog_message_extra_action_picker);

    MessageExtraActionListAdapter adapter = new MessageExtraActionListAdapter(actionItems);
    adapter.setOnItemClickListener(new MessageExtraActionListAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(int itemId) {
        callbackOnItemSelected(itemId);
        dismiss();
      }
    });

    RecyclerView recyclerView =
        (RecyclerView) dialog.findViewById(R.id.message_extra_action_listview);
    recyclerView.setAdapter(adapter);
  }

  private void callbackOnItemSelected(int itemId) {
    if (callback != null) {
      callback.onItemSelected(itemId);
    }
  }

  public interface Callback {
    void onItemSelected(int itemId);
  }
}