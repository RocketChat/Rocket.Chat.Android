package chat.rocket.android.fragment.chatroom;

import android.support.annotation.Nullable;

import java.util.List;

import chat.rocket.android.shared.BaseContract;
import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;

public interface RoomContract {

  interface View extends BaseContract.View {

    void setupWith(RocketChatAbsoluteUrl rocketChatAbsoluteUrl);

    void render(Room room);

    void showUserStatus(User user);

    void updateHistoryState(boolean hasNext, boolean isLoaded);

    void onMessageSendSuccessfully();

    void disableMessageInput();

    void enableMessageInput();

    void showUnreadCount(int count);

    void showMessages(List<Message> messages);

    void showMessageSendFailure(Message message);

    void autoloadImages();

    void manualLoadImages();

    void onReply(AbsoluteUrl absoluteUrl, String markdown, Message message);

    void onCopy(String message);

    void showMessageActions(Message message);
  }

  interface Presenter extends BaseContract.Presenter<View> {

    void loadMessages();

    void loadMoreMessages();

    void onMessageSelected(@Nullable Message message);

    void sendMessage(String messageText);

    void resendMessage(Message message);

    void updateMessage(Message message, String content);

    void deleteMessage(Message message);

    void onUnreadCount();

    void onMarkAsRead();

    void refreshRoom();

    void replyMessage(Message message);

    void copyMessage(Message message);
  }
}
