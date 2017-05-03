package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.v7.widget.RecyclerView;

import chat.rocket.android.widget.internal.RoomListItemView;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.SpotlightRoom;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  public RoomListItemViewHolder(RoomListItemView itemView,
                                RoomListAdapter.OnItemClickListener listener) {
    super(itemView);

    itemView.setOnClickListener(view -> {
      if (listener != null) {
        Object tag = view.getTag();

        if (tag instanceof Room) {
          listener.onItemClick((Room) view.getTag());
        } else if (tag instanceof SpotlightRoom) {
          listener.onItemClick((SpotlightRoom) view.getTag());
        }
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

  public void bind(SpotlightRoom spotlightRoom) {
    ((RoomListItemView) itemView)
        .setRoomId(spotlightRoom.getId())
        .setRoomName(spotlightRoom.getName())
        .setRoomType(spotlightRoom.getType())
        .setAlert(false)
        .setUnreadCount(0)
        .setTag(spotlightRoom);
  }
}
