package chat.rocket.core.repositories;

import io.reactivex.Flowable;

import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightRoom;

public interface SpotlightRoomRepository {

  Flowable<List<SpotlightRoom>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
