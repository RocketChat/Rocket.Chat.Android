package chat.rocket.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Single;

import java.util.List;
import chat.rocket.core.models.Permission;
import chat.rocket.core.models.Role;

public interface PermissionRepository {

  Single<List<Permission>> getFor(Role role);

  Single<Optional<Permission>> getById(String id);
}
