package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.rocket.android.R;
import chat.rocket.android.widget.internal.RoomListItemView;
import chat.rocket.core.models.RoomSidebar;
import chat.rocket.core.models.Spotlight;

public class RoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  public static final int MODE_ROOM = 0;
  public static final int MODE_SPOTLIGHT = 1;

  private static final int VIEW_TYPE_HEADER = 0;
  private static final int VIEW_TYPE_ROOM = 1;

  private List<RoomSidebar> roomSidebarList = Collections.emptyList();
  private List<Spotlight> spotlightList = Collections.emptyList();
  private List<RoomListHeader> roomListHeaders = Collections.emptyList();
  private Map<Integer, RoomListHeader> headersPosition = new HashMap<>();

  private int mode = MODE_ROOM;

  private OnItemClickListener externalListener;
  private OnItemClickListener listener = new OnItemClickListener() {
    @Override
    public void onItemClick(RoomSidebar roomSidebar) {
      if (externalListener != null) {
        externalListener.onItemClick(roomSidebar);
      }
    }

    @Override
    public void onItemClick(Spotlight spotlight) {
      if (externalListener != null) {
        externalListener.onItemClick(spotlight);
      }
    }
  };

  public void setRoomListHeaders(@NonNull List<RoomListHeader> roomListHeaders) {
    this.roomListHeaders = roomListHeaders;
    updateRoomSidebarList();
  }

  public void setRoomSidebarList(@NonNull List<RoomSidebar> roomSidebarList) {
    this.roomSidebarList = roomSidebarList;
    updateRoomSidebarList();
  }

  public void setSpotlightList(@NonNull List<Spotlight> spotlightList) {
    this.spotlightList = spotlightList;
    updateRoomSidebarList();
  }

  public void setMode(int mode) {
    this.mode = mode;

    if (mode == MODE_ROOM) {
      // clean up
      spotlightList.clear();
    }
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    externalListener = onItemClickListener;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_HEADER) {
      return new RoomListHeaderViewHolder(LayoutInflater
          .from(parent.getContext())
          .inflate(R.layout.room_list_header, parent, false)
      );
    }
    return new RoomListItemViewHolder(new RoomListItemView(parent.getContext()), listener);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (mode == MODE_ROOM) {
      if (getItemViewType(position) == VIEW_TYPE_HEADER) {
        ((RoomListHeaderViewHolder) holder).bind(headersPosition.get(position));
        return;
      }

      RoomSidebar roomSidebar = roomSidebarList.get(position - getTotalHeadersBeforePosition(position));
      ((RoomListItemViewHolder) holder).bind(roomSidebar);
    } else if (mode == MODE_SPOTLIGHT) {
      ((RoomListItemViewHolder) holder).bind(spotlightList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    if (mode == MODE_SPOTLIGHT) {
      return spotlightList.size();
    }
    return roomSidebarList.size() + headersPosition.size();
  }

  @Override
  public int getItemViewType(int position) {
    if (mode == MODE_SPOTLIGHT) {
      return VIEW_TYPE_ROOM;
    }

    if (headersPosition.containsKey(position)) {
      return VIEW_TYPE_HEADER;
    }
    return VIEW_TYPE_ROOM;
  }

  private void updateRoomSidebarList() {
    if (mode == MODE_ROOM) {
      sortRoomList();
      calculateHeadersPosition();
    }
    notifyDataSetChanged();
  }

  private void sortRoomList() {
    int totalHeaders = roomListHeaders.size();

    Collections.sort(roomSidebarList, (roomSidebar, anotherRoom) -> {
      for (int i = 0; i < totalHeaders; i++) {
        final RoomListHeader header = roomListHeaders.get(i);

        if (header.owns(roomSidebar) && !header.owns(anotherRoom)) {
          return -1;
        } else if (!header.owns(roomSidebar) && header.owns(anotherRoom)) {
          return 1;
        }
      }

      return roomSidebar.getRoomName().compareTo(anotherRoom.getRoomName());
    });
  }

  private void calculateHeadersPosition() {
    headersPosition.clear();

    int roomIdx = 0;
    int totalRooms = roomSidebarList.size();
    int totalHeaders = roomListHeaders.size();
    for (int i = 0; i < totalHeaders; i++) {
      final RoomListHeader header = roomListHeaders.get(i);
      if (!header.shouldShow(roomSidebarList)) {
        continue;
      }

      headersPosition.put(roomIdx + headersPosition.size(), header);

      for (; roomIdx < totalRooms; roomIdx++) {
        final RoomSidebar roomSidebar = roomSidebarList.get(roomIdx);
        if (!header.owns(roomSidebar)) {
          break;
        }
      }
    }
  }

  private int getTotalHeadersBeforePosition(int position) {
    int totalHeaders = headersPosition.size();
    Integer[] keySet = headersPosition.keySet().toArray(new Integer[totalHeaders]);

    int totalBefore = 0;
    for (int i = 0; i < totalHeaders; i++) {
      if (keySet[i] <= position) {
        totalBefore++;
      }
    }

    return totalBefore;
  }

  public interface OnItemClickListener {
    void onItemClick(RoomSidebar roomSidebar);

    void onItemClick(Spotlight spotlight);
  }
}