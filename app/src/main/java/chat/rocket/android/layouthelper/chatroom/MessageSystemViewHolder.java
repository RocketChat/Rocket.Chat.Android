package chat.rocket.android.layouthelper.chatroom;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.AbsoluteUrl;

public class MessageSystemViewHolder extends AbstractMessageViewHolder {
  private final TextView messageSystemBody;

    public MessageSystemViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
        super(itemView, absoluteUrl, hostname);
        messageSystemBody = itemView.findViewById(R.id.messageSystemBody);
    }

    @Override
    protected void bindMessage(PairedMessage pairedMessage, boolean autoLoadImage) {
        MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoLoadImage);
        messageRenderer.showAvatar(avatar, hostname);
        messageRenderer.showRealName(realName);
        messageRenderer.showUsername(username);
        messageRenderer.showMessageTimestamp(timestamp);
        messageRenderer.showSystemBody(messageSystemBody);
    }
}
