package chat.rocket.android.layouthelper;

import android.content.Context;
import android.support.annotation.LayoutRes;
import io.realm.RealmObject;

import chat.rocket.android.realm_helper.RealmModelListAdapter;
import chat.rocket.android.realm_helper.RealmModelViewHolder;

@SuppressWarnings({"PMD.AbstractNaming", "PMD.GenericsNaming"})
/**
 * RealmModelListAdapter with header and footer.
 */
public abstract class ExtRealmModelListAdapter<T extends RealmObject, VM,
    VH extends RealmModelViewHolder<VM>> extends RealmModelListAdapter<T, VM, VH> {
  protected static final int VIEW_TYPE_HEADER = -1;
  protected static final int VIEW_TYPE_FOOTER = -2;

  protected ExtRealmModelListAdapter(Context context) {
    super(context);
  }

  @Override
  public int getItemCount() {
    return super.getItemCount() + 2;
  }

  protected void notifyHeaderChanged() {
    notifyItemChanged(0);
  }

  protected void notifyFooterChanged() {
    notifyItemChanged(super.getItemCount() + 1);
  }

  protected void notifyRealmModelItemChanged(int position) {
    notifyItemChanged(position + 1);
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return VIEW_TYPE_HEADER;
    }
    if (position == super.getItemCount() + 1) {
      return VIEW_TYPE_FOOTER;
    }

    // rely on getRealmModelViewType(VM model).
    return super.getItemViewType(position - 1);
  }

  protected abstract
  @LayoutRes
  int getHeaderLayout();

  protected abstract
  @LayoutRes
  int getFooterLayout();

  protected abstract
  @LayoutRes
  int getRealmModelLayout(int viewType);

  @Override
  protected final int getLayout(int viewType) {
    if (viewType == VIEW_TYPE_HEADER) {
      return getHeaderLayout();
    }
    if (viewType == VIEW_TYPE_FOOTER) {
      return getFooterLayout();
    }

    return getRealmModelLayout(viewType);
  }

  @Override
  public final void onBindViewHolder(VH holder, int position) {
    if (position == 0 || position == super.getItemCount() + 1) {
      return;
    }

    // rely on VH.bind().
    super.onBindViewHolder(holder, position - 1);
  }
}
