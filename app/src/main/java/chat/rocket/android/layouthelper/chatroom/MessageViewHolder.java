package chat.rocket.android.layouthelper.chatroom;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_adapter.RealmModelViewHolder;

/**
 */
public class MessageViewHolder extends RealmModelViewHolder<Message> {
  private TextView text;
  public MessageViewHolder(View itemView) {
    super(itemView);
    text = (TextView) itemView.findViewById(android.R.id.text1);
  }

  public void bind(Message message) {
    text.setText(message.getMsg());
    text.setTextColor(Color.BLACK);
  }
}
