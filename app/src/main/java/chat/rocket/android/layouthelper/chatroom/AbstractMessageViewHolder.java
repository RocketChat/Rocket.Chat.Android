package chat.rocket.android.layouthelper.chatroom;

import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.helper.DateTime;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.RocketChatAvatar;
import chat.rocket.core.SyncState;

public abstract class AbstractMessageViewHolder extends ModelViewHolder<PairedMessage> {
    protected final LinearLayout dayLayout;
    protected final TextView day;
    protected final ConstraintLayout middleContainer;
    protected final RocketChatAvatar avatar;
    protected final TextView realName;
    protected final TextView username;
    protected final TextView timestamp;
    protected final ImageView errorImage;
    protected final AbsoluteUrl absoluteUrl;
    protected final String hostname;

    public AbstractMessageViewHolder(View itemView, AbsoluteUrl absoluteUrl, String hostname) {
        super(itemView);
        dayLayout = itemView.findViewById(R.id.dayLayout);
        day = itemView.findViewById(R.id.day);
        middleContainer = itemView.findViewById(R.id.middleContainer);
        avatar = itemView.findViewById(R.id.avatar);
        realName = itemView.findViewById(R.id.realName);
        username = itemView.findViewById(R.id.username);
        timestamp = itemView.findViewById(R.id.timestamp);
        errorImage = itemView.findViewById(R.id.errorImage);
        this.absoluteUrl = absoluteUrl;
        this.hostname = hostname;
    }

    /**
     * bind the view model.
     */
    public final void bind(PairedMessage pairedMessage, boolean autoloadImages) {
        if (pairedMessage.target.getSyncState() == SyncState.FAILED) {
            errorImage.setVisibility(View.VISIBLE);
        } else {
            errorImage.setVisibility(View.GONE);
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
        } else if (!pairedMessage.target.isGroupable() || !pairedMessage.nextSibling.isGroupable() || !pairedMessage.hasSameUser()) {
            setNewDay(null);
            setSequential(false);
        } else {
            setNewDay(null);
            setSequential(true);
        }
    }

    private void setNewDay(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            dayLayout.setVisibility(View.GONE);
        } else {
            day.setText(text);
            dayLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setSequential(boolean sequential) {
        if (sequential) {
            avatar.setVisibility(View.GONE);
            middleContainer.setVisibility(View.GONE);
        } else {
            avatar.setVisibility(View.VISIBLE);
            middleContainer.setVisibility(View.VISIBLE);
        }
    }
}