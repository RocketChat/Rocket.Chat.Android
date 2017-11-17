package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.ExtModelListAdapter;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.core.models.Message;

/**
 * target list adapter for chat room.
 */
public class MessageListAdapter extends ExtModelListAdapter<Message, PairedMessage, AbstractMessageViewHolder> {

  private static final int VIEW_TYPE_UNKNOWN = 0;
  private static final int VIEW_TYPE_NORMAL_MESSAGE = 1;
  private static final int VIEW_TYPE_SYSTEM_MESSAGE = 2;

  private String hostname;
  private AbsoluteUrl absoluteUrl;

  private boolean autoloadImages = false;
  private boolean hasNext;
  private boolean isLoaded;

  public MessageListAdapter(Context context, String hostname) {
    super(context);
    this.hostname = hostname;
    this.hasNext = true;
  }

  public void setAbsoluteUrl(AbsoluteUrl absoluteUrl) {
    this.absoluteUrl = absoluteUrl;
    notifyDataSetChanged();
  }

  public void setAutoloadImages(boolean autoloadImages) {
    this.autoloadImages = autoloadImages;
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
        return new MessageNormalViewHolder(itemView, absoluteUrl, hostname);
      case VIEW_TYPE_SYSTEM_MESSAGE:
        return new MessageSystemViewHolder(itemView, absoluteUrl, hostname);
      default:
        return new AbstractMessageViewHolder(itemView, absoluteUrl, hostname) {
          @Override
          protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {}
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

  @Override
  protected boolean shouldAutoloadImages() {
    return autoloadImages;
  }

  @Override
  protected DiffUtil.Callback getDiffCallback(List<PairedMessage> oldData,
                                              List<PairedMessage> newData) {
    return new PairedMessageDiffCallback(oldData, newData);
  }

  private static class PairedMessageDiffCallback extends DiffUtil.Callback {

    private final List<PairedMessage> oldList;
    private final List<PairedMessage> newList;

    public PairedMessageDiffCallback(List<PairedMessage> oldList, List<PairedMessage> newList) {
      this.oldList = oldList;
      this.newList = newList;
    }

    @Override
    public int getOldListSize() {
      if (oldList == null) {
        return 0;
      }
      return oldList.size();
    }

    @Override
    public int getNewListSize() {
      if (newList == null) {
        return 0;
      }
      return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      PairedMessage oldMessage = oldList.get(oldItemPosition);
      PairedMessage newMessage = newList.get(newItemPosition);

      return oldMessage.getId().equals(newMessage.getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      PairedMessage oldMessage = oldList.get(oldItemPosition);
      PairedMessage newMessage = newList.get(newItemPosition);

      return oldMessage.equals(newMessage);
    }
  }
}