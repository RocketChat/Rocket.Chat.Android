package chat.rocket.android.layouthelper.chatroom;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.AbsoluteUrl;

/**
 * ViewData holder of NORMAL chat message.
 */
public class MessageSystemViewHolder extends AbstractMessageViewHolder {
  private final TextView body;

  /**
   * constructor WITH hostname.
   */
  public MessageSystemViewHolder(View itemView, AbsoluteUrl absoluteUrl) {
    super(itemView, absoluteUrl);
    body = itemView.findViewById(R.id.message_body);
  }

  @Override
  protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
    new MessageRenderer(itemView.getContext(), pairedMessage.target, autoloadImages)
        .avatarInto(avatar, absoluteUrl)
        .usernameInto(username, subUsername)
        .timestampInto(timestamp);

    if (pairedMessage.target != null) {
      body.setText(MessageType.parse(pairedMessage.target.getType())
          .getString(body.getContext(), pairedMessage.target));
    }
  }
}
