package chat.rocket.android.widget.message.autocomplete.user;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import chat.rocket.android.widget.AbsoluteUrl;
import chat.rocket.android.widget.helper.UserStatusProvider;
import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.core.models.SpotlightUser;

public class UserItem implements AutocompleteItem {

  private final SpotlightUser user;
  private final AbsoluteUrl absoluteUrl;
  private final UserStatusProvider userStatusProvider;

  public UserItem(@NonNull SpotlightUser user, AbsoluteUrl absoluteUrl,
                  UserStatusProvider userStatusProvider) {
    this.user = user;
    this.absoluteUrl = absoluteUrl;
    this.userStatusProvider = userStatusProvider;
  }

  @NonNull
  @Override
  public String getSuggestion() {
    //noinspection ConstantConditions
    return user.getUsername();
  }

  public AbsoluteUrl getAbsoluteUrl() {
    return absoluteUrl;
  }

  @DrawableRes
  public int getStatusResId() {
    return userStatusProvider.getStatusResId(user.getStatus());
  }
}
