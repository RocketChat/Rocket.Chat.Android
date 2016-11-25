package chat.rocket.android.realm_helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import chat.rocket.android.realm_adapter.RealmModelListAdapter;
import chat.rocket.android.realm_adapter.RealmModelViewHolder;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmModelListView extends RecyclerView {
  private Realm realm;

  public RealmModelListView(Context context) {
    super(context);
  }

  public RealmModelListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RealmModelListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /*package*/ <T extends RealmObject, VH extends RealmModelViewHolder<T>> void setup(
      final RealmHelper realmHelper,
      final RealmModelListAdapter.Query<T> query,
      final RealmModelListAdapter.Constructor<T, VH> constructor) {
    addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
      @Override public void onViewAttachedToWindow(View view) {
        realm = realmHelper.instance();
        RealmResults<T> results = query.queryItems(realm);
        if (getAdapter() instanceof RealmModelListAdapter) {
          ((RealmModelListAdapter<T, VH>) getAdapter()).updateData(results);
        } else {
          RealmModelListAdapter<T, VH> adapter = constructor.getNewInstance(view.getContext());
          adapter.updateData(results);
          setAdapter(adapter);
        }
      }

      @Override public void onViewDetachedFromWindow(View view) {
        if (realm != null && !realm.isClosed()) {
          realm.close();
        }
      }
    });
  }

  // just for preventing from unexpected overriding.
  @Override public final void setAdapter(Adapter adapter) {
    super.setAdapter(adapter);
  }
}
