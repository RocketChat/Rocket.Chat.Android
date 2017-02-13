package chat.rocket.android.activity;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainPresenter extends BasePresenter<MainContract.View>
    implements MainContract.Presenter {

  private final CanCreateRoomInteractor canCreateRoomInteractor;

  public MainPresenter(CanCreateRoomInteractor canCreateRoomInteractor) {
    this.canCreateRoomInteractor = canCreateRoomInteractor;
  }

  @Override
  public void onOpenRoom(String hostname, String roomId) {
    final Subscription subscription = canCreateRoomInteractor.canCreate(roomId)
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(allowed -> {
          if (allowed) {
            view.showRoom(hostname, roomId);
          } else {
            view.showHome();
          }
        });

    addSubscription(subscription);
  }
}
