package chat.rocket.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Single;

import chat.rocket.core.models.Permission;

public interface PermissionRepository {

  Single<Optional<Permission>> getById(String id);
}
