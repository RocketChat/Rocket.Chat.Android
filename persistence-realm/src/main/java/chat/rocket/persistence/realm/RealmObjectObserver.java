package chat.rocket.persistence.realm;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RealmObjectObserver<T extends RealmObject> extends AbstractRealmResultsObserver<T> {

  private final RealmHelper.Query<T> query;
  private OnUpdateListener<T> onUpdateListener;
  private Impl<T> impl;
  private String previousResultString;

  /*package*/ RealmObjectObserver(RealmHelper helper, RealmHelper.Query<T> query) {
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

  @Override
  protected final RealmResults<T> queryItems(Realm realm) {
    return query.query(realm).findAll();
  }

  @Override
  protected final RealmChangeListener<RealmResults<T>> getListener() {
    return element -> {
      T currentResult = impl.extractObjectFromResults(element);
      String currentResultString = currentResult != null ? currentResult.toString() : "";
      if (previousResultString != null && previousResultString.equals(currentResultString)) {
        return;
      }
      previousResultString = currentResultString;
      if (onUpdateListener != null) {
        onUpdateListener.onUpdateObject(
            currentResult != null ? realm.copyFromRealm(currentResult) : null);
      }
    };
  }

  public void sub() {
    previousResultString = null;
    super.sub();
  }

  public interface OnUpdateListener<T extends RealmObject> {
    void onUpdateObject(T element);
  }

  public static class Impl<T extends RealmObject> {
    protected T extractObjectFromResults(RealmResults<T> results) {
      return results.isEmpty() ? null : results.last();
    }
  }
}