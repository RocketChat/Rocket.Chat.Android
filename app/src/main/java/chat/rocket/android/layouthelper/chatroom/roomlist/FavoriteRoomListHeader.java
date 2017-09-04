package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.core.models.Room;

public class FavoriteRoomListHeader implements RoomListHeader {

  private final String title;

  public FavoriteRoomListHeader(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(Room room) {
    return room.isFavorite();
  }

  @Override
  public boolean shouldShow(@NonNull List<Room> roomList) {
    for (int i = 0, size = roomList.size(); i < size; i++) {
      if (roomList.get(i).isFavorite()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public ClickListener getClickListener() {
    return null;
  }
}
