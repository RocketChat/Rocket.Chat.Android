package chat.rocket.android.layouthelper.chatroom;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

@SuppressWarnings("PMD.GenericsNaming")
public abstract class ModelListAdapter<T, VM, VH extends ModelViewHolder<VM>>
    extends RecyclerView.Adapter<VH> {

  protected final LayoutInflater inflater;
  private List<VM> adapterData;
  private OnItemClickListener<VM> onItemClickListener;

  protected ModelListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
  }

  protected abstract int getRealmModelViewType(VM model);

  @LayoutRes
  protected abstract int getLayout(int viewType);

  protected abstract VH onCreateRealmModelViewHolder(int viewType, View itemView);

  protected abstract List<VM> mapResultsToViewModel(List<T> results);

  @Override
  public int getItemViewType(int position) {
    return getRealmModelViewType(getItem(position));
  }

  @Override
  public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = inflater.inflate(getLayout(viewType), parent, false);
    return onCreateRealmModelViewHolder(viewType, itemView);
  }

  @Override
  public void onBindViewHolder(VH holder, int position) {
    VM model = getItem(position);
    holder.itemView.setTag(model);
    holder.itemView.setOnClickListener(view -> {
      VM model2 = (VM) (view.getTag());
      if (model2 != null && onItemClickListener != null) {
        onItemClickListener.onItemClick(model2);
      }
    });
    holder.bind(model);
  }

  @Override
  public int getItemCount() {
    return adapterData != null ? adapterData.size() : 0;
  }

  private VM getItem(int position) {
    return adapterData.get(position);
  }

  public void updateData(List<T> newData) {
    if (adapterData == null) {
      adapterData = mapResultsToViewModel(newData);
      notifyDataSetChanged();
    } else {
      final List<VM> newMappedData = mapResultsToViewModel(newData);
      final DiffUtil.Callback diffCallback = getDiffCallback(adapterData, newMappedData);
      final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

      adapterData = newMappedData;

      diffResult.dispatchUpdatesTo(getListUpdateCallback());
    }
  }

  protected abstract DiffUtil.Callback getDiffCallback(List<VM> oldData, List<VM> newData);

  private final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
    @Override
    public void onInserted(int position, int count) {
      notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
      notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
      notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
      notifyItemRangeChanged(position, count, payload);
    }
  };

  protected ListUpdateCallback getListUpdateCallback() {
    return listUpdateCallback;
  }

  public void setOnItemClickListener(OnItemClickListener<VM> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public interface Constructor<T, VM, VH extends ModelViewHolder<VM>> {
    ModelListAdapter<T, VM, VH> getNewInstance(Context context);
  }

  public interface OnItemClickListener<VM> {
    void onItemClick(VM model);
  }
}
