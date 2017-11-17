package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.PublicSetting;
import io.reactivex.Single;

public interface PublicSettingRepository {

  Single<Optional<PublicSetting>> getById(String id);
}
