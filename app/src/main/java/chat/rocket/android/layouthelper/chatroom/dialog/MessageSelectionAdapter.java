package chat.rocket.android.layouthelper.chatroom.dialog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.message.AbstractMessageSpec;

public class MessageSelectionAdapter
    extends RecyclerView.Adapter<MessageSelectionViewHolder> {

  private List<AbstractMessageSpec> abstractMessageSpecs = new ArrayList<>();
  private ClickListener listener;

  public void setListener(ClickListener listener) {
    this.listener = listener;
  }

  public void addMessageSpec(AbstractMessageSpec abstractMessageSpec) {
    abstractMessageSpecs.add(abstractMessageSpec);
    notifyDataSetChanged();
  }

  @Override
  public MessageSelectionViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.dialog_message_selection_item, parent, false);

    itemView.setOnClickListener(view -> {
      if (listener != null) {
        listener.onClick((AbstractMessageSpec) itemView.getTag());
      }
    });

    return new MessageSelectionViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(MessageSelectionViewHolder holder,
                               int position) {
    holder.onBind(abstractMessageSpecs.get(position));
  }

  @Override
  public int getItemCount() {
    return abstractMessageSpecs.size();
  }


  public interface ClickListener {
    void onClick(AbstractMessageSpec abstractMessageSpec);
  }
}
