package chat.rocket.android.layouthelper.chatroom;

import android.support.design.widget.FloatingActionButton;
import bolts.Task;
import chat.rocket.android.widget.message.MessageComposer;

/**
 * handling visibility of FAB-compose and MessageComposer.
 */
public class MessageComposerManager {
  public interface Callback {
    Task<Void> onSubmit(String messageText);
  }

  private final FloatingActionButton fabCompose;
  private final MessageComposer messageComposer;
  private Callback callback;

  public MessageComposerManager(FloatingActionButton fabCompose, MessageComposer messageComposer) {
    this.fabCompose = fabCompose;
    this.messageComposer = messageComposer;
    init();
  }

  private void init() {
    fabCompose.setOnClickListener(view -> {
      setMessageComposerVisibility(true);
    });

    messageComposer.setOnActionListener(new MessageComposer.ActionListener() {
      @Override public void onSubmit(String message) {
        if (callback != null) {
          messageComposer.setEnabled(false);
          callback.onSubmit(message).onSuccess(task -> {
            clearComposingText();
            return null;
          }).continueWith(task -> {
            messageComposer.setEnabled(true);
            return null;
          });
        }
      }

      @Override public void onCancel() {
        setMessageComposerVisibility(false);
      }
    });

    setMessageComposerVisibility(false);
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public void clearComposingText() {
    messageComposer.setText("");
  }

  private void setMessageComposerVisibility(boolean show) {
    if (show) {
      fabCompose.hide();
      messageComposer.show(null);
    } else {
      messageComposer.hide(fabCompose::show);
    }
  }

  public boolean hideMessageComposerIfNeeded() {
    if (messageComposer.isShown()) {
      setMessageComposerVisibility(false);
      return true;
    }
    return false;
  }
}
