package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.rocket.core.models.LoginServiceConfiguration;

public interface LoginServiceConfigurationRepository {

  Single<Optional<LoginServiceConfiguration>> getByName(String serviceName);

  Flowable<List<LoginServiceConfiguration>> getAll();
}
