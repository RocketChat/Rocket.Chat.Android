package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;
import io.reactivex.Single;

import chat.rocket.core.models.PublicSetting;

public interface PublicSettingRepository {

  Single<Optional<PublicSetting>> getById(String id);
}
