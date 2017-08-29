package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.v7.widget.RecyclerView;

import chat.rocket.android.widget.internal.RoomListItemView;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.Spotlight;
import chat.rocket.core.models.User;

public class RoomListItemViewHolder extends RecyclerView.ViewHolder {
  private RoomListItemView itemView;

  public RoomListItemViewHolder(RoomListItemView itemView, RoomListAdapter.OnItemClickListener listener) {
    super(itemView);

    this.itemView = itemView;

    itemView.setOnClickListener(view -> {
      Object object = view.getTag();
      if (object instanceof Room) {
        listener.onItemClick((Room)object);
      } else if (object instanceof Spotlight) {
        listener.onItemClick((Spotlight)object);
      }
    });
  }


  public void bind(Room room) {
    itemView
        .setRoomId(room.getRoomId())
        .setRoomName(room.getName())
        .setAlert(room.isAlert())
        .setUnreadCount(room.getUnread())
        .setTag(room);

      showRoomIcon(room.getType());
  }

  public void bind(Spotlight spotlight) {
    itemView
        .setRoomId(spotlight.getId())
        .setRoomName(spotlight.getName())
        .setAlert(false)
        .setUnreadCount(0)
        .setTag(spotlight);

    showRoomIcon(spotlight.getType());
  }

  /**
   * Only shows the room icon if it is a PRIVATE CHANNEL or PUBLIC CHANNEL, otherwise you should use {@link #bind(User)} to show the correct icon.
   * @param roomType The type of Room.
   * @see Room
   */
  private void showRoomIcon(String roomType) {
    if(!roomType.equals(Room.TYPE_DIRECT_MESSAGE)) {
      switch (roomType) {
        case Room.TYPE_CHANNEL:
          itemView.showPublicChannelIcon();
          break;
        case Room.TYPE_PRIVATE:
          itemView.showPrivateChannelIcon();
          break;
      }
    }
  }

  /**
   * Shows the user status icon.
   * @param user The user to show its status.
   * @see User
   */
  public void bind(User user) {
    String userStatus = user.getStatus();
    if (userStatus == null) {
      itemView.showOfflineUserStatusIcon();
    } else {
      switch (user.getStatus()) {
        case User.STATUS_ONLINE:
          itemView.showOnlineUserStatusIcon();
          break;
        case User.STATUS_BUSY:
          itemView.showBusyUserStatusIcon();
          break;
        case User.STATUS_AWAY:
          itemView.showAwayUserStatusIcon();
          break;
        default:
          itemView.showOfflineUserStatusIcon();
          break;
      }
    }
  }
}