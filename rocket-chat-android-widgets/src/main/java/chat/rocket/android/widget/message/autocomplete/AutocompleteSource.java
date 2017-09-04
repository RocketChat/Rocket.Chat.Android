package chat.rocket.android.widget.message.autocomplete;

import android.support.annotation.NonNull;
import io.reactivex.disposables.Disposable;

public abstract class AutocompleteSource<A extends AutocompleteAdapter, I extends AutocompleteItem> {

  protected A adapter;

  private AutocompleteSource.OnAutocompleteSelected autocompleteSelected;
  private AutocompleteViewHolder.OnClickListener autocompleteListener =
      new AutocompleteViewHolder.OnClickListener<I>() {
        @Override
        public void onClick(I autocompleteItem) {
          if (autocompleteSelected != null && autocompleteItem != null) {
            autocompleteSelected.onSelected(getAutocompleteSuggestion(autocompleteItem));
          }
        }
      };

  @NonNull
  public abstract String getTrigger();

  @NonNull
  public A getAdapter() {
    if (adapter == null) {
      adapter = createAdapter();
      adapter.setOnClickListener(autocompleteListener);
    }
    return adapter;
  }

  @NonNull
  public abstract Disposable loadList(String text);

  public abstract void dispose();

  public void setOnAutocompleteSelected(
      AutocompleteSource.OnAutocompleteSelected autocompleteSelected) {
    this.autocompleteSelected = autocompleteSelected;
  }

  protected abstract A createAdapter();

  protected abstract String getAutocompleteSuggestion(I autocompleteItem);

  public interface OnAutocompleteSelected {
    void onSelected(String autocompleteSuggestion);
  }
}
