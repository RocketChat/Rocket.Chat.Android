package chat.rocket.android.shared;

import android.support.annotation.NonNull;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public abstract class BasePresenter<T extends BaseContract.View>
    implements BaseContract.Presenter<T> {

  protected T view;
  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  @Override
  public void bindView(@NonNull T view) {
    this.view = view;
  }

  @Override
  public void release() {
    compositeSubscription.clear();
    view = null;
  }

  protected void addSubscription(Subscription subscription) {
    compositeSubscription.add(subscription);
  }

  protected void clearSubscripions() {
    compositeSubscription.clear();
  }
}
