package chat.rocket.core.repositories;

import java.util.List;

import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightUser;
import io.reactivex.Flowable;

public interface SpotlightUserRepository {

  Flowable<List<SpotlightUser>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
