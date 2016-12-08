package chat.rocket.android.realm_helper;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.realm.RealmObject;
import java.util.List;

public abstract class RealmModelListAdapter<T extends RealmObject, VM,
    VH extends RealmModelViewHolder<VM>> extends RecyclerView.Adapter<VH> {

  public interface Constructor<T extends RealmObject, VM, VH extends RealmModelViewHolder<VM>> {
    RealmModelListAdapter<T, VM, VH> getNewInstance(Context context);
  }

  public interface OnItemClickListener<VM> {
    void onItemClick(VM model);
  }

  protected final LayoutInflater inflater;
  private RealmListObserver<T> realmListObserver;
  private List<VM> adapterData;
  private OnItemClickListener<VM> onItemClickListener;

  protected RealmModelListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
  }

  /*package*/ RealmModelListAdapter<T, VM, VH> initializeWith(final RealmHelper realmHelper,
      RealmListObserver.Query<T> query) {
    realmListObserver = new RealmListObserver<>(realmHelper, query)
        .setOnUpdateListener(results -> updateData(realmHelper.copyFromRealm(results)));
    return this;
  }

  @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    realmListObserver.sub();
  }

  @Override public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    realmListObserver.unsub();
    super.onDetachedFromRecyclerView(recyclerView);
  }

  protected abstract int getRealmModelViewType(VM model);

  protected abstract @LayoutRes int getLayout(int viewType);

  protected abstract VH onCreateRealmModelViewHolder(int viewType, View itemView);

  protected abstract List<VM> mapResultsToViewModel(List<T> results);


  @Override public int getItemViewType(int position) {
    return getRealmModelViewType(getItem(position));
  }

  @Override public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = inflater.inflate(getLayout(viewType), parent, false);
    return onCreateRealmModelViewHolder(viewType, itemView);
  }

  @Override public void onBindViewHolder(VH holder, int position) {
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

  @Override public int getItemCount() {
    return adapterData != null ? adapterData.size() : 0;
  }

  private VM getItem(int position) {
    return adapterData.get(position);
  }

  private void updateData(List<T> newData) {
    if (adapterData == null) {
      adapterData = mapResultsToViewModel(newData);
      notifyDataSetChanged();
    } else {
      // TODO: use DillUtils!
      adapterData = mapResultsToViewModel(newData);
      notifyDataSetChanged();
    }
  }

  public void setOnItemClickListener(OnItemClickListener<VM> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }
}
