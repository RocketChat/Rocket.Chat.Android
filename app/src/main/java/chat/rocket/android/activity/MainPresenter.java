package chat.rocket.android.activity;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainPresenter extends BasePresenter<MainContract.View>
    implements MainContract.Presenter {

  private final CanCreateRoomInteractor canCreateRoomInteractor;
  private final RoomInteractor roomInteractor;

  public MainPresenter(RoomInteractor roomInteractor,
                       CanCreateRoomInteractor canCreateRoomInteractor) {
    this.roomInteractor = roomInteractor;
    this.canCreateRoomInteractor = canCreateRoomInteractor;
  }

  @Override
  public void bindView(@NonNull MainContract.View view) {
    super.bindView(view);

    subscribeToUnreadCount();
  }

  private void subscribeToUnreadCount() {
    final Subscription subscription = Observable.combineLatest(
        roomInteractor.getTotalUnreadRoomsCount(),
        roomInteractor.getTotalUnreadMentionsCount(),
        (Pair::new)
    )
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> view.showUnreadCount(pair.first, pair.second));

    addSubscription(subscription);
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
