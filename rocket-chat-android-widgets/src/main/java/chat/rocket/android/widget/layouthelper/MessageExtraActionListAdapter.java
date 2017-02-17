package chat.rocket.android.widget.layouthelper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.MessageExtraActionItemPresenter;

public class MessageExtraActionListAdapter
    extends RecyclerView.Adapter<MessageExtraActionItemViewHolder> {

  private final List<MessageExtraActionItemPresenter> actionItems;
  private OnItemClickListener listener;

  public MessageExtraActionListAdapter(List<MessageExtraActionItemPresenter> actionItems) {
    this.actionItems = actionItems;
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  @Override
  public MessageExtraActionItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.message_extra_action_item, parent, false);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (listener != null) {
          if (view.getTag() != null) {
            listener.onItemClick((int) view.getTag());
          }
        }
      }
    });

    return new MessageExtraActionItemViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MessageExtraActionItemViewHolder holder,
                               int position) {
    holder.onBind(actionItems.get(position));
  }

  @Override
  public int getItemCount() {
    if (actionItems == null) {
      return 0;
    }
    return actionItems.size();
  }


  public interface OnItemClickListener {
    void onItemClick(int itemId);
  }
}
