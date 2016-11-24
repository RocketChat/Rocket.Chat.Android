package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.view.View;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_adapter.RealmModelListAdapter;

/**
 * message list adapter for chat room.
 */
public class MessageListAdapter extends RealmModelListAdapter<Message, MessageViewHolder> {

  public MessageListAdapter(Context context) {
    super(context);
  }

  @Override protected int getItemViewType(Message model) {
    return 0;
  }

  @Override protected int getLayout(int viewType) {
    return android.R.layout.simple_list_item_1;
  }

  @Override protected MessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    return new MessageViewHolder(itemView);
  }
}
