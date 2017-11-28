package chat.rocket.android.layouthelper.chatroom;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.core.SyncState;

public abstract class AbstractMessageViewHolder extends ModelViewHolder<PairedMessage> {
  protected final RocketChatAvatar avatar;
  protected final ImageView errorImageView;
  protected final TextView username;
  protected final TextView subUsername;
  protected final TextView timestamp;
  protected final View userAndTimeContainer;
  protected final AbsoluteUrl absoluteUrl;
  protected final String hostname;
  protected final View newDayContainer;
  protected final TextView newDayText;

  /**
   * constructor WITH hostname.
   */
  public AbstractMessageViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
    super(itemView);
    avatar = itemView.findViewById(R.id.user_avatar);
    errorImageView = itemView.findViewById(R.id.errorImageView);
    username = itemView.findViewById(R.id.username);
    subUsername = itemView.findViewById(R.id.sub_username);
    timestamp = itemView.findViewById(R.id.timestamp);
    userAndTimeContainer = itemView.findViewById(R.id.user_and_timestamp_container);
    newDayContainer = itemView.findViewById(R.id.dayContainer);
    newDayText = itemView.findViewById(R.id.day);
    this.absoluteUrl = absoluteUrl;
    this.hostname = hostname;
  }

  /**
   * bind the view model.
   */
  public final void bind(PairedMessage pairedMessage, boolean autoloadImages) {
    if (pairedMessage.target.getSyncState() == SyncState.FAILED ||
        pairedMessage.target.getSyncState() == SyncState.DELETE_FAILED) {
      errorImageView.setVisibility(View.VISIBLE);
    } else {
      errorImageView.setVisibility(View.GONE);
    }

    bindMessage(pairedMessage, autoloadImages);
    renderNewDayAndSequential(pairedMessage);
  }

  protected abstract void bindMessage(PairedMessage pairedMessage, boolean autoloadImages);

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
    if (avatar != null) {
      if (sequential) {
        avatar.setVisibility(View.GONE);
      } else {
        avatar.setVisibility(View.VISIBLE);
      }
    }

    if (userAndTimeContainer != null) {
      if (sequential)
        userAndTimeContainer.setVisibility(View.GONE);
      else
        userAndTimeContainer.setVisibility(View.VISIBLE);
    }
  }

  private void setNewDay(@Nullable String text) {
    if (newDayContainer != null) {
      if (TextUtils.isEmpty(text)) {
        newDayContainer.setVisibility(View.GONE);
      } else {
        newDayText.setText(text);
        newDayContainer.setVisibility(View.VISIBLE);
      }
    }
  }
}
