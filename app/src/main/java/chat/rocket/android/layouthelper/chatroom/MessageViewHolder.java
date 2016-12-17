package chat.rocket.android.layouthelper.chatroom;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.realm_helper.RealmModelViewHolder;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;

/**
 * View holder of NORMAL chat message.
 */
public class MessageViewHolder extends RealmModelViewHolder<PairedMessage> {
  private final ImageView avatar;
  private final TextView username;
  private final TextView timestamp;
  private final View userAndTimeContainer;
  private final String hostname;
  private final String userId;
  private final String token;
  private final RocketChatMessageLayout body;
  private final RocketChatMessageUrlsLayout urls;
  private final RocketChatMessageAttachmentsLayout attachments;
  private final View newDayContainer;
  private final TextView newDayText;

  /**
   * constructor WITH hostname.
   */
  public MessageViewHolder(View itemView, String hostname, String userId, String token) {
    super(itemView);
    avatar = (ImageView) itemView.findViewById(R.id.user_avatar);
    username = (TextView) itemView.findViewById(R.id.username);
    timestamp = (TextView) itemView.findViewById(R.id.timestamp);
    userAndTimeContainer = itemView.findViewById(R.id.user_and_timestamp_container);
    body = (RocketChatMessageLayout) itemView.findViewById(R.id.message_body);
    urls = (RocketChatMessageUrlsLayout) itemView.findViewById(R.id.message_urls);
    attachments =
        (RocketChatMessageAttachmentsLayout) itemView.findViewById(R.id.message_attachments);
    newDayContainer = itemView.findViewById(R.id.newday_container);
    newDayText = (TextView) itemView.findViewById(R.id.newday_text);
    this.hostname = hostname;
    this.userId = userId;
    this.token = token;
  }

  /**
   * bind the view model.
   */
  public void bind(PairedMessage pairedMessage) {
    new MessageRenderer(itemView.getContext(), pairedMessage.target)
        .avatarInto(avatar, hostname)
        .usernameInto(username)
        .timestampInto(timestamp)
        .bodyInto(body)
        .urlsInto(urls)
        .attachmentsInto(attachments, hostname, userId, token);

    if (pairedMessage.target != null) {
      int syncstate = pairedMessage.target.getSyncState();
      if (syncstate == SyncState.NOT_SYNCED || syncstate == SyncState.SYNCING) {
        itemView.setAlpha(0.6f);
      } else {
        itemView.setAlpha(1.0f);
      }
    }

    renderNewDayAndSequential(pairedMessage);
  }

  private void renderNewDayAndSequential(PairedMessage pairedMessage) {
    //see Rocket.Chat:packages/rocketchat-livechat/app/client/views/message.coffee
    if (!pairedMessage.hasSameDate()) {
      setNewDay(DateTime.fromEpocMs(pairedMessage.target.getTimestamp(), DateTime.Format.DATE));
      setSequential(false);
    } else if (!pairedMessage.target.isGroupable() || !pairedMessage.nextSibling.isGroupable()
        || !pairedMessage.hasSameUser()) {
      setNewDay(null);
      setSequential(false);
    } else {
      setNewDay(null);
      setSequential(true);
    }
  }

  private void setSequential(boolean sequential) {
    if (sequential) {
      avatar.setVisibility(View.INVISIBLE);
      userAndTimeContainer.setVisibility(View.GONE);
    } else {
      avatar.setVisibility(View.VISIBLE);
      userAndTimeContainer.setVisibility(View.VISIBLE);
    }
  }

  private void setNewDay(@Nullable String text) {
    if (TextUtils.isEmpty(text)) {
      newDayContainer.setVisibility(View.GONE);
    } else {
      newDayText.setText(text);
      newDayContainer.setVisibility(View.VISIBLE);
    }
  }
}
