package chat.rocket.android.widget.message.autocomplete.user;

import android.support.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.widget.message.autocomplete.AutocompleteSource;
import chat.rocket.core.interactors.UserInteractor;
import chat.rocket.core.models.User;

public class UserSource extends AutocompleteSource<UserAdapter, UserItem> {

  private final UserInteractor userInteractor;
  private final Scheduler bgScheduler;
  private final Scheduler fgScheduler;

  public UserSource(UserInteractor userInteractor, Scheduler bgScheduler, Scheduler fgScheduler) {
    this.userInteractor = userInteractor;
    this.bgScheduler = bgScheduler;
    this.fgScheduler = fgScheduler;
  }

  @NonNull
  @Override
  public String getTrigger() {
    return "@";
  }

  @NonNull
  @Override
  public Disposable loadList(String text) {
    return Flowable.just(text)
        .map(new Function<String, String>() {
          @Override
          public String apply(@io.reactivex.annotations.NonNull String s) throws Exception {
            return s.substring(1);
          }
        })
        .flatMap(new Function<String, Publisher<List<User>>>() {
          @Override
          public Publisher<List<User>> apply(@io.reactivex.annotations.NonNull String s)
              throws Exception {
            return userInteractor.getUserAutocompleteSuggestions(s);
          }
        })
        .map(new Function<List<User>, List<UserItem>>() {
          @Override
          public List<UserItem> apply(@io.reactivex.annotations.NonNull List<User> users)
              throws Exception {
            return toUserItemList(users);
          }
        })
        .subscribeOn(bgScheduler)
        .observeOn(fgScheduler)
        .subscribe(
            new Consumer<List<UserItem>>() {
              @Override
              public void accept(@io.reactivex.annotations.NonNull List<UserItem> userItems)
                  throws Exception {
                if (adapter != null) {
                  adapter.setAutocompleteItems(userItems);
                }
              }
            },
            new Consumer<Throwable>() {
              @Override
              public void accept(@io.reactivex.annotations.NonNull Throwable throwable)
                  throws Exception {
              }
            }
        );
  }

  @Override
  public void dispose() {
    adapter = null;
  }

  @Override
  protected UserAdapter createAdapter() {
    return new UserAdapter();
  }

  @Override
  protected String getAutocompleteSuggestion(UserItem autocompleteItem) {
    return getTrigger() + autocompleteItem.getTitle();
  }

  private List<UserItem> toUserItemList(List<User> users) {
    int size = users.size();
    List<UserItem> userItems = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      userItems.add(new UserItem(users.get(i)));
    }

    return userItems;
  }
}
