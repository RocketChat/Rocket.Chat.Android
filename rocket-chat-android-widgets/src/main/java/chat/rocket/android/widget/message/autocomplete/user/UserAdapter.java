package chat.rocket.android.widget.message.autocomplete.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.autocomplete.AutocompleteAdapter;

public class UserAdapter extends AutocompleteAdapter<UserItem, UserViewHolder> {

  @Override
  public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.autocomplete_channel_view, parent, false);

    return new UserViewHolder(view, onClickListener);
  }
}
