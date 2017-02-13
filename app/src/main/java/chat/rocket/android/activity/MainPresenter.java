package chat.rocket.android.activity;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MainPresenter extends BasePresenter<MainContract.View>
    implements MainContract.Presenter {

  private final CanCreateRoomInteractor canCreateRoomInteractor;
  private final RoomInteractor roomInteractor;
  private final SessionInteractor sessionInteractor;

  public MainPresenter(RoomInteractor roomInteractor,
                       CanCreateRoomInteractor canCreateRoomInteractor,
                       SessionInteractor sessionInteractor) {
    this.roomInteractor = roomInteractor;
    this.canCreateRoomInteractor = canCreateRoomInteractor;
    this.sessionInteractor = sessionInteractor;
  }

  @Override
  public void bindView(@NonNull MainContract.View view) {
    super.bindView(view);

    subscribeToUnreadCount();
    subscribeToSession();
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

  @Override
  public void onRetryLogin() {
    final Subscription subscription = sessionInteractor.retryLogin()
        .subscribe();

    addSubscription(subscription);
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

  private void subscribeToSession() {
    final Subscription subscription = sessionInteractor.getDefault()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(session -> {
          if (session == null || session.getToken() == null) {
            view.showLoginScreen();
            return;
          }

          String error = session.getError();
          if (error != null && error.length() != 0) {
            view.showConnectionError();
            return;
          }

          if (!session.isTokenVerified()) {
            view.showConnecting();
            return;
          }

          view.showConnectionOk();
        });

    addSubscription(subscription);
  }
}
