package chat.rocket.android.widget.message.autocomplete.channel;

import android.support.annotation.NonNull;

import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.core.models.Room;

public class ChannelItem implements AutocompleteItem {

  private final Room room;

  public ChannelItem(@NonNull Room room) {
    this.room = room;
  }

  public String getTitle() {
    return room.getName();
  }
}
