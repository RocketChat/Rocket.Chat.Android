package chat.rocket.android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
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
    userRenderer = new UserRenderer(context, message.getU());
  }

  /**
   * show Avatar image.
   */
  public MessageRenderer avatarInto(ImageView imageView, String hostname) {
    switch (object.getSyncstate()) {
      case SyncState.FAILED:
        imageView.setImageResource(R.drawable.ic_error_outline_black_24dp);
        break;
      default:
        userRenderer.avatarInto(imageView, hostname);
        break;
    }
    return this;
  }

  /**
   * show Username in textView.
   */
  public MessageRenderer usernameInto(TextView textView) {
    userRenderer.usernameInto(textView);
    return this;
  }

  /**
   * show timestamp in textView.
   */
  public MessageRenderer timestampInto(TextView textView) {
    if (!shouldHandle(textView)) {
      return this;
    }

    switch (object.getSyncstate()) {
      case SyncState.NOT_SYNCED:
      case SyncState.SYNCING:
        textView.setText(R.string.sending);
        break;
      default:
        textView.setText(DateTime.fromEpocMs(object.getTs(), DateTime.Format.TIME));
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

    rocketChatMessageLayout.setText(object.getMsg());

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
      attachmentsLayout.setCredential(userId, token);
      attachmentsLayout.setAttachments(attachments);
    }

    return this;
  }

}
