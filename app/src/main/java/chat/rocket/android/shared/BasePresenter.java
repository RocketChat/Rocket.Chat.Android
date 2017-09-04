package chat.rocket.android.shared;

import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BasePresenter<T extends BaseContract.View>
    implements BaseContract.Presenter<T> {

  protected T view;
  private CompositeDisposable compositeSubscription = new CompositeDisposable();

  @Override
  public void bindView(@NonNull T view) {
    this.view = view;
  }

  @Override
  public void release() {
    compositeSubscription.clear();
    view = null;
  }

  protected void addSubscription(Disposable subscription) {
    compositeSubscription.add(subscription);
  }

  protected void clearSubscriptions() {
    compositeSubscription.clear();
  }
}
