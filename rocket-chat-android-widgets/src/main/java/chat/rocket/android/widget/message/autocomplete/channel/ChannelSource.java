package chat.rocket.android.widget.message.autocomplete.channel;

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
import chat.rocket.core.interactors.AutocompleteChannelInteractor;
import chat.rocket.core.models.SpotlightRoom;

public class ChannelSource extends AutocompleteSource<ChannelAdapter, ChannelItem> {

  private final AutocompleteChannelInteractor autocompleteChannelInteractor;
  private final Scheduler bgScheduler;
  private final Scheduler fgScheduler;

  public ChannelSource(AutocompleteChannelInteractor autocompleteChannelInteractor,
                       Scheduler bgScheduler,
                       Scheduler fgScheduler) {
    this.autocompleteChannelInteractor = autocompleteChannelInteractor;
    this.bgScheduler = bgScheduler;
    this.fgScheduler = fgScheduler;
  }

  @NonNull
  @Override
  public String getTrigger() {
    return "#";
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
        .flatMap(new Function<String, Publisher<List<SpotlightRoom>>>() {
          @Override
          public Publisher<List<SpotlightRoom>> apply(@io.reactivex.annotations.NonNull String s)
              throws Exception {
            return autocompleteChannelInteractor.getSuggestionsFor(s);
          }
        })
        .distinctUntilChanged()
        .map(new Function<List<SpotlightRoom>, List<ChannelItem>>() {
          @Override
          public List<ChannelItem> apply(
              @io.reactivex.annotations.NonNull List<SpotlightRoom> spotlightRooms)
              throws Exception {
            return toChannelItemList(spotlightRooms);
          }
        })
        .subscribeOn(bgScheduler)
        .observeOn(fgScheduler)
        .subscribe(
            new Consumer<List<ChannelItem>>() {
              @Override
              public void accept(@io.reactivex.annotations.NonNull List<ChannelItem> channelItems)
                  throws Exception {
                if (adapter != null) {
                  adapter.setAutocompleteItems(channelItems);
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
  protected ChannelAdapter createAdapter() {
    return new ChannelAdapter();
  }

  @Override
  protected String getAutocompleteSuggestion(ChannelItem autocompleteItem) {
    return getTrigger() + autocompleteItem.getSuggestion();
  }

  private List<ChannelItem> toChannelItemList(List<SpotlightRoom> spotlightRooms) {
    int size = spotlightRooms.size();
    List<ChannelItem> channelItems = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      channelItems.add(new ChannelItem(spotlightRooms.get(i)));
    }

    return channelItems;
  }
}
