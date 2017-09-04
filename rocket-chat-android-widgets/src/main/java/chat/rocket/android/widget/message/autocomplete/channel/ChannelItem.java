package chat.rocket.android.widget.message.autocomplete.channel;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import chat.rocket.android.widget.helper.IconProvider;
import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.core.models.SpotlightRoom;

public class ChannelItem implements AutocompleteItem {

  private final SpotlightRoom spotlightRoom;

  public ChannelItem(@NonNull SpotlightRoom spotlightRoom) {
    this.spotlightRoom = spotlightRoom;
  }

  @NonNull
  @Override
  public String getSuggestion() {
    return spotlightRoom.getName();
  }

  @StringRes
  public int getIcon() {
    return IconProvider.getIcon(spotlightRoom.getType());
  }
}
