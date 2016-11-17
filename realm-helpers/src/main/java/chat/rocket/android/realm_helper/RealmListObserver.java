package chat.rocket.android.realm_helper;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import java.util.List;

public class RealmListObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {
  public interface Query<T extends RealmObject> {
    RealmResults<T> queryItems(Realm realm);
  }
  public interface OnUpdateListener<T extends RealmObject> {
    void onUpdateResults(List<T> results);
  }

  private final Query<T> query;
  private OnUpdateListener<T> onUpdateListener;

  /*package*/ RealmListObserver(RealmHelper helper, Query<T> query) {
    super(helper);
    this.query = query;
  }

  public RealmListObserver<T> setOnUpdateListener(OnUpdateListener<T> onUpdateListener) {
    this.onUpdateListener = onUpdateListener;
    return this;
  }

  @Override protected final RealmResults<T> queryItems(Realm realm) {
    return query.queryItems(realm);
  }

  @Override public final RealmChangeListener<RealmResults<T>> getListener() {
    return new RealmChangeListener<RealmResults<T>>() {
      @Override public void onChange(RealmResults<T> element) {
        if (onUpdateListener != null) {
          onUpdateListener.onUpdateResults(element);
        }
      }
    };
  }
}