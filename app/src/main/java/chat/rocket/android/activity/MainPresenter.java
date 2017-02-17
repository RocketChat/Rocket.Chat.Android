package chat.rocket.android.activity;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.models.Session;

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
    final Disposable subscription = canCreateRoomInteractor.canCreate(roomId)
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
    final Disposable subscription = sessionInteractor.retryLogin()
        .subscribe();

    addSubscription(subscription);
  }

  private void subscribeToUnreadCount() {
    final Disposable subscription = Flowable.combineLatest(
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
    final Disposable subscription = sessionInteractor.getDefault()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(sessionOptional -> {
          Session session = sessionOptional.orNull();
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
