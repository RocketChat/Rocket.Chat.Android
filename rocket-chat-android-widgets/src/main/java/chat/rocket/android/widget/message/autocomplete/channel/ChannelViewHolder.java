package chat.rocket.android.widget.message.autocomplete.channel;

import android.view.View;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.message.autocomplete.AutocompleteItem;
import chat.rocket.android.widget.message.autocomplete.AutocompleteViewHolder;

public class ChannelViewHolder extends AutocompleteViewHolder<ChannelItem> {

  private final TextView titleTextView;

  public ChannelViewHolder(View itemView,
                           final AutocompleteViewHolder.OnClickListener<ChannelItem> onClickListener) {
    super(itemView);

    titleTextView = (TextView) itemView.findViewById(R.id.title);

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (onClickListener != null) {
          onClickListener.onClick((ChannelItem) v.getTag());
        }
      }
    });
  }

  @Override
  public void bind(ChannelItem channelItem) {
    itemView.setTag(channelItem);

    if (titleTextView != null) {
      titleTextView.setText(channelItem.getTitle());
    }
  }
}
