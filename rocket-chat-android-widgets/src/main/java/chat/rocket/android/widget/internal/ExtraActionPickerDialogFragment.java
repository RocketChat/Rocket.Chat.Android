package chat.rocket.android.widget.internal;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.layouthelper.MessageExtraActionListAdapter;
import chat.rocket.android.widget.message.MessageExtraActionItemPresenter;

public class ExtraActionPickerDialogFragment extends BottomSheetDialogFragment {

  private List<MessageExtraActionItemPresenter> actionItems;

  public static ExtraActionPickerDialogFragment create(
      List<MessageExtraActionItemPresenter> actionItems) {
    ExtraActionPickerDialogFragment fragment = new ExtraActionPickerDialogFragment();
    fragment.setActionItems(actionItems);

    return fragment;
  }

  public void setActionItems(List<MessageExtraActionItemPresenter> actionItems) {
    this.actionItems = actionItems;
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
    final Fragment fragment = getTargetFragment();
    if (fragment instanceof Callback) {
      ((Callback) fragment).onItemSelected(itemId);
    }
  }

  public interface Callback {
    void onItemSelected(int itemId);
  }
}