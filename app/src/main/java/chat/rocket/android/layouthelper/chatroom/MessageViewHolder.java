package chat.rocket.android.layouthelper.chatroom;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.realm_helper.RealmModelViewHolder;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.message.RocketChatMessageLayout;

/**
 * View holder of NORMAL chat message.
 */
public class MessageViewHolder extends RealmModelViewHolder<PairedMessage> {
  private final ImageView avatar;
  private final TextView username;
  private final TextView timestamp;
  private final View userAndTimeContainer;
  private final String hostname;
  private final RocketChatMessageLayout body;
  private final View newDayContainer;
  private final TextView newDayText;

  /**
   * constructor WITH hostname.
   */
  public MessageViewHolder(View itemView, String hostname) {
    super(itemView);
    avatar = (ImageView) itemView.findViewById(R.id.user_avatar);
    username = (TextView) itemView.findViewById(R.id.username);
    timestamp = (TextView) itemView.findViewById(R.id.timestamp);
    userAndTimeContainer = itemView.findViewById(R.id.user_and_timestamp_container);
    body = (RocketChatMessageLayout) itemView.findViewById(R.id.message_body);
    newDayContainer = itemView.findViewById(R.id.newday_container);
    newDayText = (TextView) itemView.findViewById(R.id.newday_text);
    this.hostname = hostname;
  }

  /**
   * bind the view model.
   */
  public void bind(PairedMessage pairedMessage) {
    new MessageRenderer(itemView.getContext(), pairedMessage.target)
        .avatarInto(avatar, hostname)
        .usernameInto(username)
        .timestampInto(timestamp)
        .bodyInto(body);

    renderNewDayAndSequential(pairedMessage);
  }

  private void renderNewDayAndSequential(PairedMessage pairedMessage) {
    //see Rocket.Chat:packages/rocketchat-livechat/app/client/views/message.coffee
    if (!pairedMessage.hasSameDate()) {
      setNewDay(DateTime.fromEpocMs(pairedMessage.target.getTs(), DateTime.Format.DATE));
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

  private void setSequential( boolean sequential) {
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
