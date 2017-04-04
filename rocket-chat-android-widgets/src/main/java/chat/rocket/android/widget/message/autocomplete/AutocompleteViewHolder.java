package chat.rocket.android.widget.message.autocomplete;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class AutocompleteViewHolder<I extends AutocompleteItem>
    extends RecyclerView.ViewHolder {
  public AutocompleteViewHolder(View itemView) {
    super(itemView);
  }

  public abstract void bind(I autocompleteItem);

  public abstract void showAsEmpty();

  public interface OnClickListener<I extends AutocompleteItem> {
    void onClick(I autocompleteItem);
  }
}
