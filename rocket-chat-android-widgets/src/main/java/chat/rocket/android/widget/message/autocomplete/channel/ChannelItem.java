package chat.rocket.android.widget.message.autocomplete.channel;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import chat.rocket.android.widget.helper.IconProvider;
import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.core.models.Room;

public class ChannelItem implements AutocompleteItem {

  private final Room room;

  public ChannelItem(@NonNull Room room) {
    this.room = room;
  }

  @NonNull
  @Override
  public String getSuggestion() {
    return room.getName();
  }

  @StringRes
  public int getIcon() {
    return IconProvider.getIcon(room.getType());
  }
}
