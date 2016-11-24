package chat.rocket.android.realm_adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public abstract class RealmModelListAdapter<T extends RealmObject,
    VH extends RealmModelViewHolder<T>> extends RealmRecyclerViewAdapter<T, VH> {

  public interface Query<T extends RealmObject> {
    RealmResults<T> queryItems(Realm realm);
  }

  public interface Constructor<T extends RealmObject, VH extends RealmModelViewHolder<T>> {
    RealmModelListAdapter<T, VH> getNewInstance(Context context);
  }

  public RealmModelListAdapter(Context context) {
    super(context, null, true);
  }

  protected abstract int getItemViewType(T model);

  protected abstract @LayoutRes int getLayout(int viewType);

  protected abstract VH onCreateRealmModelViewHolder(int viewType, View itemView);

  @Override public final int getItemViewType(int position) {
    return getItemViewType(getItem(position));
  }

  @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = inflater.inflate(getLayout(viewType), parent, false);
    return onCreateRealmModelViewHolder(viewType, itemView);
  }

  @Override public void onBindViewHolder(VH holder, int position) {
    holder.bind(getItem(position));
  }
}
