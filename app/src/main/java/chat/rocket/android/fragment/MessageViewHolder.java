package chat.rocket.android.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.view.Avatar;

/*package*/ class MessageViewHolder extends RecyclerView.ViewHolder {
    Avatar avatar;
    TextView username;
    TextView timestamp;
    TextView content;
    LinearLayout inlineContainer;
    View newDayContainer;
    TextView newDayText;
    View usertimeContainer;

    public MessageViewHolder(View itemView, String host) {
        super(itemView);
        avatar = new Avatar(host, itemView.findViewById(R.id.avatar_color), (TextView)itemView.findViewById(R.id.avatar_initials), (ImageView)itemView.findViewById(R.id.avatar_img));
        username = (TextView) itemView.findViewById(R.id.list_item_message_username);
        timestamp = (TextView) itemView.findViewById(R.id.list_item_message_timestamp);
        content = (TextView) itemView.findViewById(R.id.list_item_message_content);
        inlineContainer = (LinearLayout) itemView.findViewById(R.id.list_item_inline_container);
        newDayContainer = itemView.findViewById(R.id.list_item_message_newday);
        newDayText = (TextView) itemView.findViewById(R.id.list_item_message_newday_text);
        usertimeContainer = itemView.findViewById(R.id.list_item_message_user_time_container);
    }
}
