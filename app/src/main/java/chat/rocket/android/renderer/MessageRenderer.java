package chat.rocket.android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import chat.rocket.android.R;
import chat.rocket.android.helper.Avatar;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;

/**
 * Renderer for Message model.
 */
public class MessageRenderer extends AbstractRenderer<Message> {

  private final UserRenderer userRenderer;

  public MessageRenderer(Context context, Message message) {
    super(context, message);
    userRenderer = new UserRenderer(context, message.getUser());
  }

  /**
   * show Avatar image.
   */
  public MessageRenderer avatarInto(ImageView imageView, String hostname) {
    if (object.getSyncState() == SyncState.FAILED) {
      imageView.setImageResource(R.drawable.ic_error_outline_black_24dp);
    } else if (TextUtils.isEmpty(object.getAvatar())) {
      userRenderer.avatarInto(imageView, hostname);
    } else {
      final User user = object.getUser();
      setAvatarInto(object.getAvatar(), hostname, user == null ? null : user.getUsername(),
          imageView);
    }
    return this;
  }

  /**
   * show Username in textView.
   */
  public MessageRenderer usernameInto(TextView usernameTextView, TextView subUsernameTextView) {
    if (TextUtils.isEmpty(object.getAlias())) {
      userRenderer.usernameInto(usernameTextView);
      if (subUsernameTextView != null) {
        subUsernameTextView.setVisibility(View.GONE);
      }
    } else {
      aliasAndUsernameInto(usernameTextView, subUsernameTextView);
    }
    return this;
  }

  /**
   * show timestamp in textView.
   */
  public MessageRenderer timestampInto(TextView textView) {
    if (!shouldHandle(textView)) {
      return this;
    }

    switch (object.getSyncState()) {
      case SyncState.NOT_SYNCED:
      case SyncState.SYNCING:
        textView.setText(R.string.sending);
        break;
      default:
        textView.setText(DateTime.fromEpocMs(object.getTimestamp(), DateTime.Format.TIME));
        break;
    }

    return this;
  }

  /**
   * show body in RocketChatMessageLayout.
   */
  public MessageRenderer bodyInto(RocketChatMessageLayout rocketChatMessageLayout) {
    if (!shouldHandle(rocketChatMessageLayout)) {
      return this;
    }

    rocketChatMessageLayout.setText(object.getMessage());

    return this;
  }

  /**
   * show urls in RocketChatMessageUrlsLayout.
   */
  public MessageRenderer urlsInto(RocketChatMessageUrlsLayout urlsLayout) {
    if (!shouldHandle(urlsLayout)) {
      return this;
    }

    String urls = object.getUrls();
    if (TextUtils.isEmpty(urls)) {
      urlsLayout.setVisibility(View.GONE);
    } else {
      urlsLayout.setVisibility(View.VISIBLE);
      urlsLayout.setUrls(urls);
    }

    return this;
  }

  /**
   * show urls in RocketChatMessageUrlsLayout.
   */
  public MessageRenderer attachmentsInto(RocketChatMessageAttachmentsLayout attachmentsLayout,
                                         String hostname, String userId, String token) {
    if (!shouldHandle(attachmentsLayout)) {
      return this;
    }

    String attachments = object.getAttachments();
    if (TextUtils.isEmpty(attachments)) {
      attachmentsLayout.setVisibility(View.GONE);
    } else {
      attachmentsLayout.setVisibility(View.VISIBLE);
      attachmentsLayout.setHostname(hostname);
      attachmentsLayout.setAttachments(attachments);
    }

    return this;
  }

  private void setAvatarInto(String avatar, String hostname, String username, ImageView imageView) {
    Picasso.with(context)
        .load(avatar)
        .placeholder(
            new Avatar(hostname, username).getTextDrawable(context))
        .into(imageView);
  }

  private void aliasAndUsernameInto(TextView aliasTextView, TextView usernameTextView) {
    if (shouldHandle(aliasTextView)) {
      aliasTextView.setText(object.getAlias());
    }

    if (shouldHandle(usernameTextView)) {
      if (object.getUser() != null) {
        usernameTextView.setText("@" + object.getUser().getUsername());
        usernameTextView.setVisibility(View.VISIBLE);
      } else {
        usernameTextView.setVisibility(View.GONE);
      }
    }
  }

}
