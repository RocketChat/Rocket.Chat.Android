package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.v7.widget.RecyclerView;

import chat.rocket.android.widget.internal.RoomListItemView;
import chat.rocket.core.models.Room;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  public RoomListItemViewHolder(RoomListItemView itemView,
                                RoomListAdapter.OnItemClickListener listener) {
    super(itemView);

    itemView.setOnClickListener(view -> {
      if (listener != null) {
        listener.onItemClick((Room) view.getTag());
      }
    });
  }

  public void bind(Room room) {
    ((RoomListItemView) itemView)
        .setRoomId(room.getRoomId())
        .setRoomName(room.getName())
        .setRoomType(room.getType())
        .setAlert(room.isAlert())
        .setUnreadCount(room.getUnread())
        .setTag(room);
  }
}
