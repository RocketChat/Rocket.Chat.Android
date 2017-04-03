package chat.rocket.android.widget.message.autocomplete.user;

import android.support.annotation.NonNull;

import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.core.models.User;

public class UserItem implements AutocompleteItem {

  private final User user;

  public UserItem(@NonNull User user) {
    this.user = user;
  }

  public String getTitle() {
    return user.getUsername();
  }
}
