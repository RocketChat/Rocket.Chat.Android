package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.core.models.Room;

public class ChannelRoomListHeader implements RoomListHeader {

  private final String title;
  private final ClickListener clickListener;

  public ChannelRoomListHeader(String title, ClickListener clickListener) {
    this.title = title;
    this.clickListener = clickListener;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(Room room) {
    return room.isChannel() || room.isPrivate();
  }

  @Override
  public boolean shouldShow(@NonNull List<Room> roomList) {
    return true;
  }

  @Override
  public ClickListener getClickListener() {
    return clickListener;
  }
}
