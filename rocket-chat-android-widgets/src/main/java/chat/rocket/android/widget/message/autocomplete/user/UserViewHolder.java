package chat.rocket.android.widget.message.autocomplete.user;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;

public class UserViewHolder extends AutocompleteViewHolder<UserItem> {

  private final TextView titleTextView;

  public UserViewHolder(View itemView,
                        final AutocompleteViewHolder.OnClickListener<UserItem> onClickListener) {
    super(itemView);

    titleTextView = (TextView) itemView.findViewById(R.id.title);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onClickListener != null) {
          onClickListener.onClick((UserItem) v.getTag());
        }
      }
    });
  }

  @Override
  public void bind(UserItem userItem) {
    itemView.setTag(userItem);

    if (titleTextView != null) {
      titleTextView.setText(userItem.getTitle());
    }
  }
}
