package chat.rocket.android.widget.message.autocomplete;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class AutocompleteAdapter<I extends AutocompleteItem, H extends AutocompleteViewHolder<I>>
    extends RecyclerView.Adapter<H> {

  private List<I> autocompleteItems = new ArrayList<>();
  protected AutocompleteViewHolder.OnClickListener<I> onClickListener;

  public void setAutocompleteItems(List<I> autocompleteItems) {
    this.autocompleteItems.clear();
    this.autocompleteItems.addAll(autocompleteItems);
    notifyDataSetChanged();
  }

  @Override
  public void onBindViewHolder(H holder, int position) {
    holder.bind(autocompleteItems.get(position));
  }

  @Override
  public int getItemCount() {
    return autocompleteItems.size();
  }

  public void setOnClickListener(AutocompleteViewHolder.OnClickListener<I> onClickListener) {
    this.onClickListener = onClickListener;
  }
}
