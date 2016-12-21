package chat.rocket.android.layouthelper.chatroom.dialog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.message.MessageSpec;

public class MessageSelectionAdapter
    extends RecyclerView.Adapter<MessageSelectionViewHolder> {

  private List<MessageSpec> messageSpecs = new ArrayList<>();
  private ClickListener listener;

  public void setListener(ClickListener listener) {
    this.listener = listener;
  }

  public void addMessageSpec(MessageSpec messageSpec) {
    messageSpecs.add(messageSpec);
    notifyDataSetChanged();
  }

  @Override
  public MessageSelectionViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.dialog_message_selection_item, parent, false);

    itemView.setOnClickListener(v -> {
      if (listener != null) {
        listener.onClick((MessageSpec) itemView.getTag());
      }
    });

    return new MessageSelectionViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MessageSelectionViewHolder holder,
                               int position) {
    holder.onBind(messageSpecs.get(position));
  }

  @Override
  public int getItemCount() {
    return messageSpecs.size();
  }


  public interface ClickListener {
    void onClick(MessageSpec messageSpec);
  }
}
