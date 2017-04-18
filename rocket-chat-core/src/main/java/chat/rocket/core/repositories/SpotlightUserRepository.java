package chat.rocket.core.repositories;

import io.reactivex.Flowable;

import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightUser;

public interface SpotlightUserRepository {

  Flowable<List<SpotlightUser>> getSuggestionsFor(String name, SortDirection direction, int limit);
}
