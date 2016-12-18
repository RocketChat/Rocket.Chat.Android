package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;

import chat.rocket.android.R;
import chat.rocket.android.model.ddp.Message;

/**
 * message type.
 */
public enum MessageType {
  ROOM_NAME_CHANGED("r") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_room_name_changed,
          message.getMessage(), message.getUser().getUsername());
    }
  },
  USER_ADDED("au") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_added_by,
          message.getMessage(), message.getUser().getUsername());
    }
  },
  USER_REMOVED("ru") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_removed_by,
          message.getMessage(), message.getUser().getUsername());
    }
  },
  USER_JOINED("uj") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_joined_channel);
    }
  },
  USER_LEFT("ul") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_user_left);
    }
  },
  WELCOME("wm") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_welcome, message.getUser().getUsername());
    }
  },
  MESSAGE_REMOVED("rm") {
    @Override
    public String getString(Context context, Message message) {
      return context.getString(R.string.message_removed);
    }
  },
  UNSPECIFIED("");
  //------------

  private final String value;

  MessageType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static MessageType parse(String value) {
    for (MessageType type : MessageType.values()) {
      if (type.value.equals(value)) return type;
    }
    return UNSPECIFIED;
  }

  public String getString(Context context, Message message) {
    return "";
  }
}
