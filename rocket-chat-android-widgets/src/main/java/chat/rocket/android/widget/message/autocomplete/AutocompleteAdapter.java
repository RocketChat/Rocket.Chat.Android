package chat.rocket.android.widget.message.autocomplete;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class AutocompleteAdapter<I extends AutocompleteItem, H extends AutocompleteViewHolder<I>>
    extends RecyclerView.Adapter<H> {

  private static final int TYPE_EMPTY = 0;
  private static final int TYPE_ITEM = 1;

  private List<I> autocompleteItems = new ArrayList<>();
  protected AutocompleteViewHolder.OnClickListener<I> onClickListener;

  public void setAutocompleteItems(List<I> autocompleteItems) {
    this.autocompleteItems.clear();
    this.autocompleteItems.addAll(autocompleteItems);
    notifyDataSetChanged();
  }

  public H onCreateViewHolder(ViewGroup parent, int viewType) {
    H holder = getViewHolder(parent);

    if (viewType == TYPE_EMPTY) {
      holder.showAsEmpty();
    }

    return holder;
  }

  @Override
  public void onBindViewHolder(H holder, int position) {
    if (getItemViewType(position) == TYPE_EMPTY) {
      return;
    }
    holder.bind(autocompleteItems.get(position));
  }

  @Override
  public int getItemCount() {
    int count = autocompleteItems.size();
    if (count == 0) {
      return 1;
    }
    return count;
  }

  @Override
  public int getItemViewType(int position) {
    if (autocompleteItems.size() == 0) {
      return TYPE_EMPTY;
    }
    return TYPE_ITEM;
  }

  public abstract H getViewHolder(ViewGroup parent);

  public void setOnClickListener(AutocompleteViewHolder.OnClickListener<I> onClickListener) {
    this.onClickListener = onClickListener;
  }
}
