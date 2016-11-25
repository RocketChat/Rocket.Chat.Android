package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.view.View;
import chat.rocket.android.R;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_helper.RealmModelListAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * target list adapter for chat room.
 */
public class MessageListAdapter
    extends RealmModelListAdapter<Message, PairedMessage, MessageViewHolder> {

  private final String hostname;

  public MessageListAdapter(Context context, String hostname) {
    super(context);
    this.hostname = hostname;
  }

  @Override protected int getRealmModelViewType(PairedMessage model) {
    return 0;
  }

  @Override protected int getLayout(int viewType) {
    return R.layout.list_item_message;
  }

  @Override protected MessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    return new MessageViewHolder(itemView, hostname);
  }

  @Override protected List<PairedMessage> mapResultsToViewModel(List<Message> results) {
    if (results.isEmpty()) {
      return Collections.emptyList();
    }

    ArrayList<PairedMessage> extMessages = new ArrayList<>();
    for (int i = 0; i < results.size() - 1; i++) {
      extMessages.add(new PairedMessage(results.get(i), results.get(i + 1)));
    }
    extMessages.add(new PairedMessage(results.get(results.size() - 1), null));

    return extMessages;
  }
}
