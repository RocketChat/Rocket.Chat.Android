package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.v7.widget.RecyclerView;

import chat.rocket.android.widget.internal.RoomListItemView;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Spotlight;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  public RoomListItemViewHolder(RoomListItemView itemView,
                                RoomListAdapter.OnItemClickListener listener) {
    super(itemView);

    itemView.setOnClickListener(view -> {
      if (listener != null) {
        Object tag = view.getTag();

        if (tag instanceof Room) {
          listener.onItemClick((Room) view.getTag());
        } else if (tag instanceof Spotlight) {
          listener.onItemClick((Spotlight) view.getTag());
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

  public void bind(Spotlight spotlight) {
    ((RoomListItemView) itemView)
        .setRoomId(spotlight.getId())
        .setRoomName(spotlight.getName())
        .setRoomType(spotlight.getType())
        .setAlert(false)
        .setUnreadCount(0)
        .setTag(spotlight);
  }
}
