package chat.rocket.core.repositories;

import java.util.List;

import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightRoom;
import io.reactivex.Flowable;

public interface SpotlightRoomRepository {

  Flowable<List<SpotlightRoom>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
