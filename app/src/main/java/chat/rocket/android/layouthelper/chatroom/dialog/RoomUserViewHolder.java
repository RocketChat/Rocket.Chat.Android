package chat.rocket.android.layouthelper.chatroom.dialog;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import chat.rocket.android.R;

/**
 * ViewHolder for UsersOfRoom.
 */
public class RoomUserViewHolder extends RecyclerView.ViewHolder {
  ImageView status;
  ImageView avatar;
  TextView username;

  public RoomUserViewHolder(View itemView) {
    super(itemView);
    status = (ImageView) itemView.findViewById(R.id.room_user_status);
    avatar = (ImageView) itemView.findViewById(R.id.room_user_avatar);
    username = (TextView) itemView.findViewById(R.id.room_user_name);
  }
}
