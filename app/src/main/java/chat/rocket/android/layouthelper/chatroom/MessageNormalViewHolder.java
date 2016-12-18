package chat.rocket.android.layouthelper.chatroom;

import android.view.View;

import chat.rocket.android.R;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;

/**
 * View holder of NORMAL chat message.
 */
public class MessageNormalViewHolder extends AbstractMessageViewHolder {
  private final RocketChatMessageLayout body;
  private final RocketChatMessageUrlsLayout urls;
  private final RocketChatMessageAttachmentsLayout attachments;

  /**
   * constructor WITH hostname.
   */
  public MessageNormalViewHolder(View itemView, String hostname, String userId, String token) {
    super(itemView, hostname, userId, token);
    body = (RocketChatMessageLayout) itemView.findViewById(R.id.message_body);
    urls = (RocketChatMessageUrlsLayout) itemView.findViewById(R.id.message_urls);
    attachments =
        (RocketChatMessageAttachmentsLayout) itemView.findViewById(R.id.message_attachments);
  }

  @Override
  protected void bindMessage(PairedMessage pairedMessage) {
    new MessageRenderer(itemView.getContext(), pairedMessage.target)
        .avatarInto(avatar, hostname)
        .usernameInto(username)
        .timestampInto(timestamp)
        .bodyInto(body)
        .urlsInto(urls)
        .attachmentsInto(attachments, hostname, userId, token);
  }
}
