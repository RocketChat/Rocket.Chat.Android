package chat.rocket.android.layouthelper.chatroom;

import android.view.View;

import chat.rocket.android.R;
import chat.rocket.android.renderer.MessageRenderer;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout;
import chat.rocket.android.widget.message.RocketChatMessageLayout;
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout;

public class MessageNormalViewHolder extends AbstractMessageViewHolder {
    private final RocketChatMessageLayout rocketChatMessageLayout;
    private final RocketChatMessageUrlsLayout rocketChatMessageUrlsLayout;
    private final RocketChatMessageAttachmentsLayout rocketChatMessageAttachmentsLayout;

    public MessageNormalViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
        super(itemView, absoluteUrl, hostname);
        rocketChatMessageLayout = itemView.findViewById(R.id.messageBody);
        rocketChatMessageUrlsLayout = itemView.findViewById(R.id.messageUrl);
        rocketChatMessageAttachmentsLayout = itemView.findViewById(R.id.messageAttachment);
    }

    @Override
    protected void bindMessage(PairedMessage pairedMessage, boolean autoloadImages) {
        MessageRenderer messageRenderer = new MessageRenderer(pairedMessage.target, autoloadImages);
        messageRenderer.showAvatar(avatar, hostname);
        messageRenderer.showRealName(realName);
        messageRenderer.showUsername(username);
        messageRenderer.showMessageTimestamp(timestamp);
        messageRenderer.showBody(rocketChatMessageLayout);
        messageRenderer.showUrl(rocketChatMessageUrlsLayout);
        messageRenderer.showAttachment(rocketChatMessageAttachmentsLayout, absoluteUrl);
    }
}