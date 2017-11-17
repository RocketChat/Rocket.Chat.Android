package chat.rocket.android.layouthelper.chatroom;

import android.view.View;

import chat.rocket.android.R;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;

/**
 * ViewData holder of NORMAL chat message.
 */
public class MessageNormalViewHolder extends AbstractMessageViewHolder {
  private final RocketChatMessageLayout body;
  private final RocketChatMessageUrlsLayout urls;
  private final RocketChatMessageAttachmentsLayout attachments;

  /**
   * constructor WITH hostname.
   */
  public MessageNormalViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
    super(itemView, absoluteUrl, hostname);
    body = itemView.findViewById(R.id.message_body);
    urls = itemView.findViewById(R.id.message_urls);
    attachments = itemView.findViewById(R.id.message_attachments);
  }

  @Override
  protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
    MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoloadImages);
    messageRenderer.showAvatar(avatar, hostname);
    messageRenderer.showUsername(username, subUsername);
    messageRenderer.showTimestampOrMessageState(timestamp);
    messageRenderer.showBody(body);
    messageRenderer.showUrl(urls);
    messageRenderer.showAttachment(attachments, absoluteUrl);
  }
}
