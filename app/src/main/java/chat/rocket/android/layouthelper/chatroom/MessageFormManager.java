package chat.rocket.android.layouthelper.chatroom;

import java.util.HashMap;
import bolts.Task;
import chat.rocket.android.layouthelper.extra_action.AbstractExtraActionItem;
import chat.rocket.android.widget.message.MessageFormLayout;

/**
 * handling MessageForm.
 */
public class MessageFormManager {
  private final MessageFormLayout messageFormLayout;
  private SendMessageCallback sendMessageCallback;
  private ExtraActionPickerCallback extraActionPickerCallback;
  private final HashMap<Integer, AbstractExtraActionItem> extraActionItemMap;

  public MessageFormManager(MessageFormLayout messageFormLayout) {
    this.messageFormLayout = messageFormLayout;
    this.extraActionItemMap = new HashMap<>();
    init();
  }

  private void init() {
    messageFormLayout.setOnActionListener(new MessageFormLayout.ActionListener() {
      @Override
      public void onSubmitText(String message) {
        sendMessage(message);
      }

      @Override
      public void onExtraActionSelected(int itemId) {
        if (extraActionItemMap.containsKey(itemId)) {
          AbstractExtraActionItem item = extraActionItemMap.get(itemId);
          if (extraActionPickerCallback != null) {
            extraActionPickerCallback.onExtraActionSelected(item);
          }
        }
      }
    });
  }

  public void setSendMessageCallback(SendMessageCallback sendMessageCallback) {
    this.sendMessageCallback = sendMessageCallback;
  }

  public void setExtraActionPickerCallback(ExtraActionPickerCallback extraActionPickerCallback) {
    this.extraActionPickerCallback = extraActionPickerCallback;
  }

  public void clearComposingText() {
    messageFormLayout.setText("");
  }

  private void sendMessage(String message) {
    if (sendMessageCallback == null) {
      return;
    }

    messageFormLayout.setEnabled(false);
    sendMessageCallback.onSubmitText(message).onSuccess(task -> {
      clearComposingText();
      return null;
    }).continueWith(task -> {
      messageFormLayout.setEnabled(true);
      return null;
    });
  }

  public void registerExtraActionItem(AbstractExtraActionItem actionItem) {
    messageFormLayout.addExtraActionItem(actionItem);
    extraActionItemMap.put(actionItem.getItemId(), actionItem);
  }

  public interface SendMessageCallback {
    Task<Void> onSubmitText(String messageText);
  }

  public interface ExtraActionPickerCallback {
    void onExtraActionSelected(AbstractExtraActionItem item);
  }
}
