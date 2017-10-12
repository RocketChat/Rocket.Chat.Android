package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;
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

    void showMessageDeleteFailure(Message message);

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

    void onMessageTap(@Nullable Message message);

    void sendMessage(String messageText);

    void resendMessage(@NonNull Message message);

    void updateMessage(@NonNull Message message, String content);

    void deleteMessage(@NonNull Message message);

    void onUnreadCount();

    void onMarkAsRead();

    void refreshRoom();

    void replyMessage(@NonNull Message message, boolean justQuote);

    void acceptMessageDeleteFailure(Message message);
  }
}
