package chat.rocket.android.realm_helper;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RealmObjectObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {

  public interface Query<T extends RealmObject> {
    RealmQuery<T> query(Realm realm);
  }

  public interface OnUpdateListener<T extends RealmObject> {
    void onUpdateObject(T element);
  }

  public static class Impl<T extends RealmObject> {
    protected T extractObjectFromResults(RealmResults<T> results) {
      return results.isEmpty() ? null : results.last();
    }

    protected boolean isSame(T element1, T element2) {
      return false;
    }
  }

  private final Query<T> query;
  private OnUpdateListener<T> onUpdateListener;
  private Impl<T> impl;

  /*package*/ RealmObjectObserver(RealmHelper helper, Query<T> query) {
    super(helper);
    this.query = query;
    setImpl(new Impl<T>());
  }

  public void setImpl(Impl<T> impl) {
    this.impl = impl;
  }

  public RealmObjectObserver<T> setOnUpdateListener(OnUpdateListener<T> onUpdateListener) {
    this.onUpdateListener = onUpdateListener;
    return this;
  }

  private T previousResult;

  @Override protected final RealmResults<T> queryItems(Realm realm) {
    return query.query(realm).findAll();
  }

  @Override protected final RealmChangeListener<RealmResults<T>> getListener() {
    return new RealmChangeListener<RealmResults<T>>() {
      @Override public void onChange(RealmResults<T> element) {
        T source = impl.extractObjectFromResults(element);
        T target = source != null ? realm.copyFromRealm(source) : null;
        if (previousResult != null && impl.isSame(previousResult, target)) {
          return;
        }
        previousResult = target;
        if (onUpdateListener != null) {
          onUpdateListener.onUpdateObject(target);
        }
      }
    };
  }


  public void sub() {
    previousResult = null;
    super.sub();
  }
}