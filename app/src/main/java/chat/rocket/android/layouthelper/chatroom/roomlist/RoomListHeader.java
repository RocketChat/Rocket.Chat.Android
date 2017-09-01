package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import chat.rocket.core.models.RoomSidebar;
import java.util.List;

public interface RoomListHeader {

  String getTitle();

  boolean owns(RoomSidebar roomSidebar);

  boolean shouldShow(@NonNull List<RoomSidebar> roomSidebarList);

  ClickListener getClickListener();

  interface ClickListener {
    void onClick();
  }
}
