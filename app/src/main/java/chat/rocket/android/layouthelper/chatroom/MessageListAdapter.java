package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.layouthelper.ExtRealmModelListAdapter;
import chat.rocket.android.model.ddp.Message;

/**
 * target list adapter for chat room.
 */
public class MessageListAdapter
    extends ExtRealmModelListAdapter<Message, PairedMessage, MessageViewHolder> {
  private final String hostname;
  private final String userId;
  private final String token;
  private boolean hasNext;
  private boolean isLoaded;

  public MessageListAdapter(Context context, String hostname, String userId, String token) {
    super(context);
    this.hostname = hostname;
    this.userId = userId;
    this.token = token;
  }

  /**
   * update Footer state considering hasNext and isLoaded.
   */
  public void updateFooter(boolean hasNext, boolean isLoaded) {
    this.hasNext = hasNext;
    this.isLoaded = isLoaded;
    notifyFooterChanged();
  }

  @Override
  protected int getHeaderLayout() {
    return R.layout.list_item_message_header;
  }

  @Override
  protected int getFooterLayout() {
    if (!hasNext || isLoaded) {
      return R.layout.list_item_message_start_of_conversation;
    } else {
      return R.layout.list_item_message_loading;
    }
  }

  @Override
  protected int getRealmModelViewType(PairedMessage model) {
    return 0;
  }

  @Override
  protected int getRealmModelLayout(int viewType) {
    return R.layout.list_item_message;
  }

  @Override
  protected MessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    return new MessageViewHolder(itemView, hostname, userId, token);
  }

  @Override
  protected List<PairedMessage> mapResultsToViewModel(List<Message> results) {
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
