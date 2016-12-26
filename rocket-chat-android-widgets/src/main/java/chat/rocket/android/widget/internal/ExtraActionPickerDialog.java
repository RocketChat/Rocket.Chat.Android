package chat.rocket.android.widget.internal;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.layouthelper.MessageExtraActionListAdapter;
import chat.rocket.android.widget.message.MessageExtraActionItemPresenter;

public class ExtraActionPickerDialog {
  /**
   * show extra actions picker diaog.
   */
  public static Task<Integer> showAsTask(Context context,
                                         List<MessageExtraActionItemPresenter> actionItems) {
    final TaskCompletionSource<Integer> task = new TaskCompletionSource<>();
    Impl dialog = new Impl(context, actionItems);
    dialog.setCallback(new Impl.Callback() {
      @Override
      public void onItemSelected(int itemId) {
        task.setResult(itemId);
      }

      @Override
      public void onCanceled() {
        task.setError(new OperationCanceledException());
      }
    });
    dialog.show();

    return task.getTask();
  }

  private static class Impl extends BottomSheetDialog {
    private interface Callback {
      void onItemSelected(int itemId);

      void onCanceled();
    }

    private Callback callback;
    private final List<MessageExtraActionItemPresenter> actionItems;

    public void setCallback(Callback callback) {
      this.callback = callback;
    }

    public Impl(@NonNull Context context, List<MessageExtraActionItemPresenter> actionItems) {
      super(context);
      this.actionItems = actionItems;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.dialog_message_extra_action_picker);
      MessageExtraActionListAdapter adapter = new MessageExtraActionListAdapter(actionItems);
      adapter.setOnItemClickListener(new MessageExtraActionListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int itemId) {
          doCallback(itemId);
          dismiss();
        }
      });
      RecyclerView recyclerView = (RecyclerView) findViewById(R.id.message_extra_action_listview);
      recyclerView.setAdapter(adapter);

      setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
          if (callback != null) {
            callback.onCanceled();
          }
        }
      });
    }

    private void doCallback(int itemId) {
      if (callback != null) {
        callback.onItemSelected(itemId);
      }
    }
  }
}