package chat.rocket.persistence.realm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

import java.util.Collections;
import java.util.List;

/**
 * ListAdapter for AutoCompleteTextView.
 */
public abstract class RealmAutoCompleteAdapter<T extends RealmObject> extends ArrayAdapter<T> {
  private final int textViewResourceId;
  private RealmHelper realmHelper;
  private AutoCompleteFilter filter;

  /**
   * NOTE
   * getStringForSelectedItem(T model) is automatically set to the TextView(id=textViewResourceId).
   */
  protected RealmAutoCompleteAdapter(Context context, int resource, int textViewResourceId) {
    super(context, resource, textViewResourceId);
    this.textViewResourceId = textViewResourceId;
  }

  protected void filterList(List<T> items, String text) {
  }

  /*package*/ RealmAutoCompleteAdapter<T> initializeWith(RealmHelper realmHelper,
                                                         RealmFilter<T> itemFilter) {
    this.realmHelper = realmHelper;
    this.filter = new AutoCompleteFilter<T>(this, itemFilter);
    return this;
  }

  @NonNull
  @Override
  public Filter getFilter() {
    return filter;
  }

  @NonNull
  @Override
  public final View getView(int position, View convertView, @NonNull ViewGroup parent) {
    View itemView = super.getView(position, convertView, parent);

    T item = getItem(position);
    TextView textView = (TextView) itemView.findViewById(textViewResourceId);
    textView.setText(getStringForSelectedItem(item));
    onBindItemView(itemView, item);
    return itemView;
  }

  protected abstract void onBindItemView(View itemView, T model);

  protected abstract String getStringForSelectedItem(T model);

  public interface Constructor<T extends RealmObject> {
    RealmAutoCompleteAdapter<T> getNewInstance(Context context);
  }

  public interface RealmFilter<T extends RealmObject> {
    RealmResults<T> filterItems(Realm realm, String text);
  }

  /**
   * Filter for completion.
   */
  private class AutoCompleteFilter<T extends RealmObject> extends Filter {
    private final RealmAutoCompleteAdapter<T> adapter;
    private final RealmFilter<T> realmFilter;

    /*package*/ AutoCompleteFilter(RealmAutoCompleteAdapter<T> adapter,
                                   RealmFilter<T> realmFilter) {
      this.adapter = adapter;
      this.realmFilter = realmFilter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
      FilterResults results = new FilterResults();
      if (TextUtils.isEmpty(charSequence)) {
        results.values = Collections.emptyList();
        results.count = 0;
        return results;
      }

      String text = charSequence.toString();
      List<T> filteredItems = realmHelper.executeTransactionForReadResults(realm ->
          realmFilter.filterItems(realm, text));
      adapter.filterList(filteredItems, text);
      results.values = filteredItems;
      results.count = filteredItems.size();

      return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
      adapter.clear();
      if (filterResults.count > 0) {
        adapter.addAll((List<T>) filterResults.values);
      }
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
      return adapter.getStringForSelectedItem((T) resultValue);
    }
  }
}
