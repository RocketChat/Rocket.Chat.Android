package chat.rocket.android.layouthelper.chatroom;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_adapter.RealmModelViewHolder;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.message.RocketChatMessageLayout;

/**
 */
public class MessageViewHolder extends RealmModelViewHolder<Message> {
  private final ImageView avatar;
  private final TextView username;
  private final TextView timestamp;
  private final String hostname;
  private final RocketChatMessageLayout body;

  public MessageViewHolder(View itemView, String hostname) {
    super(itemView);
    avatar = (ImageView) itemView.findViewById(R.id.user_avatar);
    username = (TextView) itemView.findViewById(R.id.username);
    timestamp = (TextView) itemView.findViewById(R.id.timestamp);
    body = (RocketChatMessageLayout) itemView.findViewById(R.id.message_body);
    this.hostname = hostname;
  }

  public void bind(Message message) {
    new MessageRenderer(itemView.getContext(), message)
        .avatarInto(avatar, hostname)
        .usernameInto(username)
        .timestampInto(timestamp)
        .bodyInto(body);
  }
}
