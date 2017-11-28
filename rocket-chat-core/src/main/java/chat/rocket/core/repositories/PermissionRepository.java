package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.Permission;
import io.reactivex.Single;

public interface PermissionRepository {

  Single<Optional<Permission>> getById(String id);
}
