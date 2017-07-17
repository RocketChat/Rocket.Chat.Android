package chat.rocket.android.renderer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;
import chat.rocket.core.SyncState;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.WebContent;
import java.util.List;

/**
 * Renderer for RealmMessage model.
 */
public class MessageRenderer extends AbstractRenderer<Message> {

  private final UserRenderer userRenderer;
  private final boolean autoloadImages;

  public MessageRenderer(Context context, Message message, boolean autoloadImages) {
    super(context, message);
    userRenderer = new UserRenderer(context, message.getUser());
    this.autoloadImages = autoloadImages;
  }

  /**
   * show Avatar image.
   */
  public MessageRenderer avatarInto(RocketChatAvatar rocketChatAvatar, AbsoluteUrl absoluteUrl) {
    if (!shouldHandle(rocketChatAvatar)) {
      return this;
    }

    switch (object.getSyncState()){
      case SyncState.FAILED:
        userRenderer.avatarInto(rocketChatAvatar, absoluteUrl, true);
        break;
      default:
        if (TextUtils.isEmpty(object.getAvatar())) {
          userRenderer.avatarInto(rocketChatAvatar, absoluteUrl, false);
        } else {
          rocketChatAvatar.loadImage(object.getAvatar());
        }
        break;
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
      case SyncState.SYNCING:
        textView.setText(R.string.sending);
        break;
      case SyncState.NOT_SYNCED:
        textView.setText(R.string.not_synced);
        break;
      case SyncState.FAILED:
        textView.setText(R.string.failed_to_sync);
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

    List<WebContent> webContents = object.getWebContents();
    if (webContents == null || webContents.size() == 0) {
      urlsLayout.setVisibility(View.GONE);
    } else {
      urlsLayout.setVisibility(View.VISIBLE);
      urlsLayout.setUrls(webContents, autoloadImages);
    }
    return this;
  }

  /**
   * show urls in RocketChatMessageUrlsLayout.
   */
  public MessageRenderer attachmentsInto(RocketChatMessageAttachmentsLayout attachmentsLayout, AbsoluteUrl absoluteUrl) {
    if (!shouldHandle(attachmentsLayout)) {
      return this;
    }

    List<Attachment> attachments = object.getAttachments();
    if (attachments == null || attachments.size() == 0) {
      attachmentsLayout.setVisibility(View.GONE);
    } else {
      attachmentsLayout.setVisibility(View.VISIBLE);
      attachmentsLayout.setAbsoluteUrl(absoluteUrl);
      attachmentsLayout.setAttachments(attachments, autoloadImages);
    }
    return this;
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