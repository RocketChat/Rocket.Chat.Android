package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.core.models.Room;

public class UnreadRoomListHeader implements RoomListHeader {

  private final String title;

  public UnreadRoomListHeader(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(Room room) {
    return room.isAlert();
  }

  @Override
  public boolean shouldShow(@NonNull List<Room> roomList) {
    for (int i = 0, size = roomList.size(); i < size; i++) {
      if (roomList.get(i).isAlert()) {
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
