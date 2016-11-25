package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.view.View;
import chat.rocket.android.R;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_adapter.RealmModelListAdapter;

/**
 * message list adapter for chat room.
 */
public class MessageListAdapter extends RealmModelListAdapter<Message, MessageViewHolder> {

  private final String hostname;

  public MessageListAdapter(Context context, String hostname) {
    super(context);
    this.hostname = hostname;
  }

  @Override protected int getItemViewType(Message model) {
    return 0;
  }

  @Override protected int getLayout(int viewType) {
    return R.layout.list_item_message;
  }

  @Override protected MessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    return new MessageViewHolder(itemView, hostname);
  }
}
