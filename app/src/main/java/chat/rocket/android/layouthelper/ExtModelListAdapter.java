package chat.rocket.android.layouthelper;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.util.ListUpdateCallback;

import chat.rocket.android.layouthelper.chatroom.ModelListAdapter;
import chat.rocket.android.layouthelper.chatroom.ModelViewHolder;

@SuppressWarnings({"PMD.AbstractNaming", "PMD.GenericsNaming"})
/**
 * ModelListAdapter with header and footer.
 */
public abstract class ExtModelListAdapter<T, VM,
    VH extends ModelViewHolder<VM>> extends ModelListAdapter<T, VM, VH> {
  protected static final int VIEW_TYPE_HEADER = -1;
  protected static final int VIEW_TYPE_FOOTER = -2;

  protected ExtModelListAdapter(Context context) {
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

  private final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
    @Override
    public void onInserted(int position, int count) {
      notifyItemRangeInserted(position + 1, count);
    }

    @Override
    public void onRemoved(int position, int count) {
      notifyItemRangeRemoved(position + 1, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
      notifyItemMoved(fromPosition + 1, toPosition + 1);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
      notifyItemRangeChanged(position + 1, count, payload);
    }
  };

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

  @Override
  protected ListUpdateCallback getListUpdateCallback() {
    return listUpdateCallback;
  }
}
