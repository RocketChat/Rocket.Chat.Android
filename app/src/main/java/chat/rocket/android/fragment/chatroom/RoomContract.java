package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;

import chat.rocket.android.model.core.Room;

public interface RoomContract {

  interface View {

    void render(Room room);

    void updateHistoryState(boolean hasNext, boolean isLoaded);
  }

  interface Presenter {
    void bindView(@NonNull View view);

    void release();
  }
}
