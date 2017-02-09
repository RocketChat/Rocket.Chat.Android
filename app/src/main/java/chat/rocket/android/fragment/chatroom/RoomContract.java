package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;

import chat.rocket.android.model.core.Room;

public interface RoomContract {

  interface View {

    void render(Room room);

    void updateHistoryState(boolean hasNext, boolean isLoaded);

    void onMessageSendSuccessfully();
  }

  interface Presenter {
    void bindView(@NonNull View view);

    void release();

    void loadMessages();

    void loadMoreMessages();

    void sendMessage(String messageText);

    void resendMessage(String messageId);

    void deleteMessage(String messageId);
  }
}
