package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.ExtRealmModelListAdapter;
import chat.rocket.android.model.ddp.Message;

/**
 * target list adapter for chat room.
 */
public class MessageListAdapter
    extends ExtRealmModelListAdapter<Message, PairedMessage, AbstractMessageViewHolder> {

  private static final int VIEW_TYPE_UNKNOWN = 0;
  private static final int VIEW_TYPE_NORMAL_MESSAGE = 1;
  private static final int VIEW_TYPE_SYSTEM_MESSAGE = 2;

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
    if (model.target != null) {
      if (TextUtils.isEmpty(model.target.getType())) {
        return VIEW_TYPE_NORMAL_MESSAGE;
      } else {
        return VIEW_TYPE_SYSTEM_MESSAGE;
      }
    }
    return VIEW_TYPE_UNKNOWN;
  }

  @Override
  protected int getRealmModelLayout(int viewType) {
    switch (viewType) {
      case VIEW_TYPE_NORMAL_MESSAGE:
        return R.layout.list_item_normal_message;
      case VIEW_TYPE_SYSTEM_MESSAGE:
        return R.layout.list_item_system_message;
      default:
        return R.layout.simple_screen;
    }
  }

  @Override
  protected AbstractMessageViewHolder onCreateRealmModelViewHolder(int viewType, View itemView) {
    switch (viewType) {
      case VIEW_TYPE_NORMAL_MESSAGE:
        return new MessageNormalViewHolder(itemView, hostname, userId, token);
      case VIEW_TYPE_SYSTEM_MESSAGE:
        return new MessageSystemViewHolder(itemView, hostname, userId, token);
      default:
        return new AbstractMessageViewHolder(itemView, hostname, userId, token) {
          @Override
          protected void bindMessage(PairedMessage pairedMessage) {
          }
        };
    }
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
