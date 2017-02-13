package chat.rocket.android.fragment.chatroom;

import android.support.annotation.Nullable;

import java.util.List;
import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;

public interface RoomContract {

  interface View extends BaseContract.View {

    void render(Room room);

    void updateHistoryState(boolean hasNext, boolean isLoaded);

    void onMessageSendSuccessfully();

    void showUnreadCount(int count);

    void showMessages(List<Message> messages);

    void showMessageSendFailure(Message message);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void loadMessages();

    void loadMoreMessages();

    void onMessageSelected(@Nullable Message message);

    void sendMessage(String messageText);

    void resendMessage(Message message);

    void deleteMessage(Message message);

    void onUnreadCount();

    void onMarkAsRead();
  }
}
