package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;

import java.util.List;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;

public interface RoomContract {

  interface View {

    void render(Room room);

    void updateHistoryState(boolean hasNext, boolean isLoaded);

    void onMessageSendSuccessfully();

    void showUnreadCount(int count);

    void showMessages(List<Message> messages);
  }

  interface Presenter {
    void bindView(@NonNull View view);

    void release();

    void loadMessages();

    void loadMoreMessages();

    void sendMessage(String messageText);

    void resendMessage(String messageId);

    void deleteMessage(String messageId);

    void onUnreadCount();

    void onMarkAsRead();
  }
}
