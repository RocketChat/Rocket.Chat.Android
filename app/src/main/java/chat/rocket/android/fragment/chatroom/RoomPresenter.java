package chat.rocket.android.fragment.chatroom;

import android.support.annotation.NonNull;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.repositories.RoomRepository;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class RoomPresenter implements RoomContract.Presenter {

  private final String roomId;
  private final RoomRepository roomRepository;

  private CompositeSubscription compositeSubscription = new CompositeSubscription();
  private RoomContract.View view;

  public RoomPresenter(String roomId, RoomRepository roomRepository) {
    this.roomId = roomId;
    this.roomRepository = roomRepository;
  }

  @Override
  public void bindView(@NonNull RoomContract.View view) {
    this.view = view;

    getRoomInfo();
    getRoomHistoryStateInfo();
  }

  @Override
  public void release() {
    compositeSubscription.unsubscribe();
    this.view = null;
  }

  private void getRoomInfo() {
    final Subscription subscription = roomRepository.getById(roomId)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            room -> view.render(room)
        );

    compositeSubscription.add(subscription);
  }

  private void getRoomHistoryStateInfo() {
    final Subscription subscription = roomRepository.getHistoryStateByRoomId(roomId)
        .distinctUntilChanged()
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            roomHistoryState -> {
              int syncState = roomHistoryState.getSyncState();
              view.updateHistoryState(
                  !roomHistoryState.isComplete(),
                  syncState == SyncState.SYNCED || syncState == SyncState.FAILED
              );
            }
        );

    compositeSubscription.add(subscription);
  }
}
